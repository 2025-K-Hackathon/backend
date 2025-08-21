import sys, json, os, io
from pathlib import Path
from datetime import datetime

sys.stdin = io.TextIOWrapper(sys.stdin.buffer, encoding='utf-8')
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

BASE_DIR = Path(__file__).resolve().parent.parent  # <project-root>/python
sys.path.append(str(BASE_DIR))
os.chdir(BASE_DIR)  # ★ 꼭 있어야 상대경로 정상

from policy_rec import get_policy_recommendations

def main():
    raw = sys.stdin.read()
    user_profile = json.loads(raw) if raw else {}

    # get_policy_recommendations 호출
    response = get_policy_recommendations(user_profile)
    ai_recommendation = response.get("ai_recommendation", "")
    formatted_docs = response.get("source_documents", [])

    result = {
        "user_profile": user_profile,
        "ai_recommendation": ai_recommendation,
        "source_documents": formatted_docs,
        "timestamp": datetime.now().isoformat()
    }

    # stdout 버퍼로 JSON만 출력
    sys.stdout.buffer.write(json.dumps(result, ensure_ascii=False, separators=(",", ":")).encode("utf-8"))
    sys.stdout.flush()

if __name__ == "__main__":
    main()
