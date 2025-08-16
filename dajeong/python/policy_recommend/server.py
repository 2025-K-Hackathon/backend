from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from .policy_rec import get_policy_recommendations, _get_db, _get_embeddings

app = FastAPI()

class Profile(BaseModel):
    name: str
    nationality: str
    region: str
    age: int
    married: bool
    hasChildren: bool
    childAge: int | None = None

@app.on_event("startup")
async def warmup():
    _get_embeddings()           # 임베딩 모델 메모리에 1회 로드
    if _get_db() is None:
        raise RuntimeError("DB 폴더가 없습니다. DB_DIRECTORY 확인")

@app.get("/health")
def health():
    return {"ok": True}

@app.post("/recommend")
def recommend(p: Profile):
    res = get_policy_recommendations(p.model_dump())
    if "error" in res:
        raise HTTPException(status_code=400, detail=res["error"])
    return res
