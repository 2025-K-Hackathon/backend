import os
import json
from datetime import datetime
from dotenv import load_dotenv
from langchain_openai import ChatOpenAI
from langchain_community.embeddings import SentenceTransformerEmbeddings
from langchain_community.vectorstores import Chroma
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.runnables import RunnableParallel, RunnablePassthrough
from langchain_core.output_parsers import StrOutputParser

# 검색된 문서(Document 객체)들을 하나의 깔끔한 문자열로 합치는 함수
def format_docs(docs):
    return "\n\n".join(f"정책 제목: {doc.metadata.get('title', '제목 없음')}\n내용: {doc.page_content}" for doc in docs)

def get_age(birth_year):
    return datetime.now().year - birth_year

def get_policy_recommendations(user_profile: dict) -> dict:
    load_dotenv()

    try:
        API_KEY = os.getenv("API_KEY")
        MODEL_ID = os.getenv("MODEL_ID")
        API_BASE = os.getenv("API_BASE")
        if not API_KEY or not MODEL_ID or not API_BASE:
            raise KeyError
    except KeyError as e:
        return {"error": str(e)}

    # 결과를 저장할 딕셔너리
    result = {
        "user_profile": user_profile,
        "ai_recommendation": "",
        "source_documents": [],
        "timestamp": datetime.now().isoformat()
    }

    DB_DIRECTORY = "./policy_chroma_db"
    if not os.path.exists(DB_DIRECTORY):
        result["error"] = f"오류: '{DB_DIRECTORY}' 데이터베이스 폴더를 찾을 수 없습니다."
        return result

    print("로컬 임베딩 모델을 로드합니다...")
    embeddings = SentenceTransformerEmbeddings(
        model_name="jhgan/ko-sbert-nli"
    )

    db = Chroma(persist_directory=DB_DIRECTORY, embedding_function=embeddings)
    print("\n2. ChromaDB 로드 완료!")

    search_regions = ["전국", user_profile["region"]]

    metadata_filter = {
        "region": {"$in": search_regions}
    }

    base_query = (
        f"{user_profile.get('name')}은/는 "
        f"{user_profile.get('nationality')} 국적의 "
        f"{get_age(user_profile.get('age'))}세 여성입니다. "
        f"현재 {user_profile.get('region')}에 거주하고 있습니다."
    )
    if user_profile.get('hasChildren') and user_profile.get('childAge') is not None:
        query_text = base_query + f" 만 {get_age(user_profile.get('childAge'))}세 자녀를 둔 어머니입니다."
    else:
        query_text = base_query + " 자녀는 없습니다."

    print(f"\n3. 생성된 AI 검색어: \"{query_text}\"")
    print(f"   적용된 DB 필터: {metadata_filter}")

    # DB에서 필터와 검색어에 맞는 문서를 찾아오는 retriever 설정
    retriever = db.as_retriever(
        search_type="similarity",
        search_kwargs={'k': 3, 'filter': metadata_filter}
    )

    llm = ChatOpenAI(
        model_name=MODEL_ID,
        openai_api_key=API_KEY,
        openai_api_base=API_BASE,
        model_kwargs={"temperature": 0.5, "max_tokens": 1024}
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
        source_documents=retriever, #  retriever를 통해 찾은 원본 문서를 그대로 반환
    )

    print("\n4. RAG Chain 준비 완료. 이제 AI에게 정책 추천을 요청합니다...")

    response = rag_chain.invoke(query_text)

    # AI 추천 결과 저장
    result["ai_recommendation"] = response["answer"]

    # 소스 문서 정보 저장
    for doc in response["source_documents"]:
        doc_info = {
            "source": doc.metadata.get('source'),
            "title": doc.metadata.get('title'),
            "conSeq": doc.metadata.get('conSeq'),
            "content": doc.page_content[:200] + "..." if len(doc.page_content) > 200 else doc.page_content  # 내용 미리보기
        }
        result["source_documents"].append(doc_info)

    return result

if __name__ == "__main__":
    # 테스트할 가상 사용자 프로필 정의
    sample_user_profile = {
        "name": "린 응우엔",
        "nationality": "베트남",
        "age": 1998,
        "region": "서울",
        "married": True,
        "hasChildren": True,
        "childAge": 2020,
    }
    result = get_policy_recommendations(sample_user_profile)
    print(json.dumps(result, ensure_ascii=False, indent=2))

if __name__ == "__main__":
    # STDIN 으로 user_profile JSON 을 받아 실행하는 래퍼 호환
    try:
        raw = json.load(sys.stdin)  # {"user_profile": {...}} 또는 그 자체 dict
        profile = raw.get("user_profile", raw)
    except Exception:
        profile = {}

    out = get_policy_recommendations(profile if isinstance(profile, dict) else {})
    print(json.dumps(out, ensure_ascii=False, indent=2))
