import os
import json
from datetime import datetime
from dotenv import load_dotenv

from langchain_openai import ChatOpenAI, OpenAIEmbeddings
# 로컬 임베딩 사용
from langchain_community.embeddings import SentenceTransformerEmbeddings
from langchain_chroma import Chroma

from langchain_core.prompts import ChatPromptTemplate
from openai import OpenAI
from langchain_core.runnables import RunnableLambda

from langchain_core.output_parsers import StrOutputParser


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
        "timestamp": datetime.now().isoformat()
    }

    headers = {
        "HTTP-Referer": os.getenv("OR_REFERER", "https://local"),
        "X-Title": os.getenv("OR_TITLE", "Dajeong"),
    }

    # DB 경로: 환경변수 우선, 없으면 기본 폴더
    DB_DIRECTORY = os.getenv("DB_DIRECTORY", "./policy_chroma_db")
    if not os.path.exists(DB_DIRECTORY):
        result["error"] = f"오류: '{DB_DIRECTORY}' 데이터베이스 폴더를 찾을 수 없습니다."
        return result

    print("로컬 임베딩 모델을 로드합니다...")
    embeddings = OpenAIEmbeddings(
        model=os.getenv("EMBED_MODEL", "text-embedding-3-small"),
        api_key=os.getenv("OPENAI_API_KEY"),
        base_url=os.getenv("API_BASE"),
        default_headers=headers,
    )

    db = Chroma(persist_directory=DB_DIRECTORY, embedding_function=embeddings)
    print("\n2. ChromaDB 로드 완료!")

    region = user_profile.get("region")
    search_regions = ["전국"] + ([region] if region else [])
    metadata_filter = {"region": {"$in": search_regions}}

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

    retriever = db.as_retriever(
        search_type="similarity",
        search_kwargs={"k": 3, "filter": metadata_filter}
    )

    llm = ChatOpenAI(
        model=MODEL_ID,
        api_key=API_KEY,
        base_url=API_BASE,
        temperature=0.5,
        max_tokens=1024,
        timeout=30,
        max_retries=3,
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
            {"context": retriever | format_docs, "question": RunnablePassthrough()}
            | prompt
            | llm
            | StrOutputParser()
        ),
        source_documents=retriever,
    )

    print("\n4. RAG Chain 준비 완료. 이제 AI에게 정책 추천을 요청합니다...")

    try:
        response = rag_chain.invoke(query_text)
    except Exception as e:
        result["error"] = f"LLM 호출 실패: {e}"
        return result

    result["ai_recommendation"] = response.get("answer", "")

    for doc in response.get("source_documents", []):
        content = doc.page_content
        if len(content) > 200:
            content = content[:200] + "..."
        result["source_documents"].append({
            "source": doc.metadata.get("source"),
            "title": doc.metadata.get("title"),
            "conSeq": doc.metadata.get("conSeq"),
            "content": content
        })

    return result

