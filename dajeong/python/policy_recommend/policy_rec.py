import os
import sys
import json
from datetime import datetime
from dotenv import load_dotenv

# ▼ 로컬 임베딩 (외부 API 불필요)
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_chroma import Chroma

from langchain_core.prompts import ChatPromptTemplate
from openai import OpenAI


def format_docs(docs):
    return "\n\n".join(
        f"정책 제목: {doc.metadata.get('title', '제목 없음')}\n내용: {doc.page_content}"
        for doc in docs
    )


def get_age(birth_year):
    try:
        return datetime.now().year - int(birth_year)
    except Exception:
        return -1


def get_policy_recommendations(user_profile: dict) -> dict:
    """
    로컬 임베딩(HuggingFaceEmbeddings) + Chroma + OpenAI(Chat) 단일 호출.
    외부 임베딩 API 미사용 → '.data' 에러 제거.
    """
    load_dotenv()

    API_KEY = os.getenv("OPENAI_API_KEY")
    MODEL_ID = os.getenv("MODEL_ID")
    API_BASE = os.getenv("API_BASE")
    if not API_KEY or not MODEL_ID or not API_BASE:
        return {"error": "환경변수(API_KEY, MODEL_ID, API_BASE) 누락"}

    result = {
        "user_profile": user_profile,
        "ai_recommendation": "",
        "source_documents": [],
        "timestamp": datetime.now().isoformat(),
    }

    # DB 경로 확인
    DB_DIRECTORY = os.getenv("DB_DIRECTORY", "./policy_chroma_db")
    if not os.path.exists(DB_DIRECTORY):
        result["error"] = f"오류: '{DB_DIRECTORY}' 데이터베이스 폴더를 찾을 수 없습니다."
        return result

    print("로컬 임베딩 모델을 로드합니다... (HuggingFaceEmbeddings)")
    # 한국어/다국어에 무난한 경량 모델 예시 (필요시 환경변수로 바꾸세요)
    HF_EMBED_MODEL = os.getenv(
        "HF_EMBED_MODEL",
        "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
    )
    try:
        embeddings = HuggingFaceEmbeddings(model_name=HF_EMBED_MODEL)
    except Exception as e:
        result["error"] = f"로컬 임베딩 로드 실패: {e}"
        return result

    try:
        db = Chroma(persist_directory=DB_DIRECTORY, embedding_function=embeddings)
    except Exception as e:
        result["error"] = f"Chroma 초기화 실패: {e}"
        return result

    print("\n2. ChromaDB 로드 완료!")

    # 지역 필터
    region = user_profile.get("region")
    search_regions = ["전국"] + ([region] if region else [])
    metadata_filter = {"region": {"$in": search_regions}}

    # 질의문 생성
    base_query = (
        f"{user_profile.get('name')}은/는 "
        f"{user_profile.get('nationality')} 국적의 "
        f"{get_age(user_profile.get('age'))}세 여성입니다. "
        f"현재 {user_profile.get('region')}에 거주하고 있습니다."
    )
    if user_profile.get("hasChildren") and user_profile.get("childAge") is not None:
        query_text = base_query + f" 만 {get_age(user_profile.get('childAge'))}세 자녀를 둔 어머니입니다."
    else:
        query_text = base_query + " 자녀는 없습니다."

    print(f'\n3. 생성된 AI 검색어: "{query_text}"')
    print(f"   적용된 DB 필터: {metadata_filter}")

    # 리트리버
    retriever = db.as_retriever(
        search_type="similarity",
        search_kwargs={"k": 3, "filter": metadata_filter},
    )

    # Chat 호출 클라이언트 (OpenRouter 헤더 포함)
    headers = {
        "HTTP-Referer": os.getenv("OR_REFERER", "https://github.com"),
        "X-Title": os.getenv("OR_TITLE", "Dajeong"),
    }
    client = OpenAI(api_key=API_KEY, base_url=API_BASE, default_headers=headers)

    # 프롬프트 (환경변수로 교체 가능)
    PROMPT_TEMPLATE = os.getenv("PROMPT_TEMPLATE", "[Context]\n{context}\n\n[User]\n{question}\n\n[Answer in Korean]:")
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
    prompt = ChatPromptTemplate.from_template(PROMPT_TEMPLATE)

    print("\n4. RAG 컨텍스트 구성 완료. 이제 AI에게 정책 추천을 요청합니다...")

    try:
        # 1) 문서 검색 (여기서 임베딩이 호출되지만 로컬이라 네트워크 이슈 없음)
        docs = retriever.invoke(query_text)  # List[Document]
        context_text = format_docs(docs)

        # 2) 프롬프트 문자열 생성
        prompt_text = prompt.format(context=context_text, question=query_text).to_string()

        # 3) Chat Completions 호출
        r = client.chat.completions.create(
            model=MODEL_ID,
            messages=[{"role": "user", "content": prompt_text}],
            max_tokens=1024,
            temperature=0.5,
        )
        content = r.choices[0].message.content or ""
        answer_text = content.strip()

        # 4) 결과 정리
        result["ai_recommendation"] = answer_text

        for doc in docs:
            content = doc.page_content
            if len(content) > 200:
                content = content[:200] + "..."
            result["source_documents"].append(
                {
                    "source": doc.metadata.get("source"),
                    "title": doc.metadata.get("title"),
                    "conSeq": doc.metadata.get("conSeq"),
                    "content": content,
                }
            )

        return result

    except Exception as e:
        result["error"] = f"LLM 호출 실패: {e}"
        return result


if __name__ == "__main__":
    # STDIN 으로 user_profile JSON 을 받아 실행하는 래퍼 호환
    try:
        raw = json.load(sys.stdin)  # {"user_profile": {...}} 또는 그 자체 dict
        profile = raw.get("user_profile", raw)
    except Exception:
        profile = {}

    out = get_policy_recommendations(profile if isinstance(profile, dict) else {})
    print(json.dumps(out, ensure_ascii=False, indent=2))
