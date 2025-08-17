# python/policy_recommend/policy_rec.py
import os
import json
from pathlib import Path
from datetime import datetime
from functools import lru_cache

from dotenv import load_dotenv
from langchain_openai import ChatOpenAI
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_chroma import Chroma
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.runnables import RunnableParallel, RunnablePassthrough
from langchain_core.output_parsers import StrOutputParser

BASE_DIR = Path(__file__).resolve().parent
DEFAULT_DB_DIR = BASE_DIR / "policy_chroma_db"

def _now_iso():
    return datetime.now().isoformat()

def _format_docs(docs):
    return "\n\n".join(
        f"정책 제목: {doc.metadata.get('title','제목 없음')}\n내용: {doc.page_content}"
        for doc in docs
    )

def _age_from_year(birth_year: int) -> int:
    try:
        return datetime.now().year - int(birth_year)
    except Exception:
        return -1

@lru_cache(maxsize=1)
def _get_embeddings():
    # 경량 다국어 모델 (메모리 절약)
    return HuggingFaceEmbeddings(
        model_name="sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2",
        model_kwargs={"device": "cpu"},
        encode_kwargs={"normalize_embeddings": True},
    )

@lru_cache(maxsize=1)
def _get_db():
    db_dir = Path(os.getenv("DB_DIRECTORY") or DEFAULT_DB_DIR)
    if not db_dir.exists():
        return None
    return Chroma(persist_directory=str(db_dir), embedding_function=_get_embeddings())

def _build_llm():
    api_key = os.getenv("API_KEY")
    model_id = os.getenv("MODEL_ID")
    api_base = os.getenv("API_BASE")

    if not api_key or not model_id or not api_base:
        raise RuntimeError("환경변수(API_KEY, MODEL_ID, API_BASE)가 비어 있습니다.")

    # 최신 인자 사용(경고 제거), 타임아웃/재시도 명시
    return ChatOpenAI(
        api_key=api_key,
        base_url=api_base,
        model=model_id,
        temperature=0.2,
        max_tokens=800,
        timeout=30,
        max_retries=3,
    )

def get_policy_recommendations(user_profile: dict) -> dict:
    """
    서버/래퍼 양쪽에서 호출되는 단일 진입점.
    실패해도 예외를 던지지 않고 JSON으로 에러를 반환합니다.
    """
    load_dotenv()
    result = {
        "user_profile": user_profile,
        "ai_recommendation": "",
        "source_documents": [],
        "timestamp": _now_iso(),
    }

    # --- 환경 및 DB 체크 ---
    try:
        llm = _build_llm()
    except Exception as e:
        result["error"] = f"LLM 설정 오류: {e}"
        return result

    db = _get_db()
    if db is None:
        result["error"] = f"오류: '{DEFAULT_DB_DIR.name}' 데이터베이스 폴더를 찾을 수 없습니다. (env: DB_DIRECTORY로 변경 가능)"
        return result

    # --- 질의 텍스트 생성 ---
    try:
        base_query = (
            f"{user_profile.get('name')}은/는 "
            f"{user_profile.get('nationality')} 국적의 "
            f"{_age_from_year(user_profile.get('age'))}세 여성입니다. "
            f"현재 {user_profile.get('region')}에 거주하고 있습니다."
        )
        if user_profile.get("hasChildren") and user_profile.get("childAge") is not None:
            query_text = base_query + f" 만 {_age_from_year(user_profile.get('childAge'))}세 자녀를 둔 어머니입니다."
        else:
            query_text = base_query + " 자녀는 없습니다."
    except Exception:
        query_text = json.dumps(user_profile, ensure_ascii=False)

    # --- 검색 필터 구성 ---
    search_regions = ["전국", user_profile.get("region")]
    metadata_filter = {"region": {"$in": [r for r in search_regions if r]}}

    retriever = db.as_retriever(
        search_type="similarity",
        search_kwargs={"k": 3, "filter": metadata_filter},
    )

    prompt = ChatPromptTemplate.from_template("""
    You are a kind and competent policy recommendation AI for the 'Dajeong' service.
    Based on the user's situation and the provided context, find the most helpful policies and list up to a maximum of 3 in order of recommendation.

    For each recommended policy, you must provide:

    A brief, easy-to-understand summary of its key points.
    A list of 2-3 keywords explaining its relevance to the user's situation.
    Please format your response for each policy exactly as follows:

    1. [Policy Title]

    Summary(In Korean): [Brief, easy-to-understand summary of the policy]
    Keywords(In Korean): [Keyword 1], [Keyword 2]

    Your mission is to answer ONLY based on the information found within the provided 'Context'.
    You must NEVER mention, guess, or create information that is not explicitly stated in the 'Context'.
    If the 'Context' contains no relevant information for the user's situation, you must honestly reply with the exact following Korean sentence: "죄송하지만, 현재 사용자님의 상황에 꼭 맞는 정책 정보를 찾지 못했습니다."

    [Context]
    {context}

    [User Situation]
    {question}

    [Personalized Policy Recommendation Based on Context]

    """)

    rag_chain = RunnableParallel(
        answer=(
            {"context": retriever | _format_docs, "question": RunnablePassthrough()}
            | prompt
            | llm
            | StrOutputParser()
        ),
        source_documents=retriever,
    )

    try:
        response = rag_chain.invoke(query_text)
    except Exception as e:
        # 외부 모델 503/과부하 등 모든 예외를 JSON으로 안전 반환
        result["error"] = f"LLM 호출 실패: {e}"
        return result

    # 정상 응답
    result["ai_recommendation"] = response.get("answer", "")
    for doc in response.get("source_documents", []):
        preview = doc.page_content
        if len(preview) > 200:
            preview = preview[:200] + "..."
        result["source_documents"].append(
            {
                "source": doc.metadata.get("source"),
                "title": doc.metadata.get("title"),
                "conSeq": doc.metadata.get("conSeq"),
                "content": preview,
            }
        )
    return result
