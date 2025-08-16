import sys, json, os, io
from pathlib import Path

sys.stdin = io.TextIOWrapper(sys.stdin.buffer, encoding='utf-8')
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

BASE_DIR = Path(__file__).resolve().parent.parent  # <project-root>/python
sys.path.append(str(BASE_DIR))
os.chdir(BASE_DIR)  # ★ 꼭 있어야 상대경로 정상

from policy_recommend.policy_rec import get_policy_recommendations

def main():
    raw = sys.stdin.read()
    user_profile = json.loads(raw) if raw else {}
    result = get_policy_recommendations(user_profile)
    print(json.dumps(result, ensure_ascii=False))

if __name__ == "__main__":
    main()
