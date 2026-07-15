# 개발 환경 셋업

새 PC에서 이 저장소를 처음 세팅할 때 확인하는 문서다. 코드/규칙 문서가 아니라 로컬 환경 구성 절차만 다룬다.

---

## 1. 사전 요구 사항

| 도구 | 용도 | 비고 |
|---|---|---|
| Git | 저장소 clone | - |
| GitHub CLI (`gh`) | PR 생성/조회 | clone 후 `gh auth login` 필요 |
| Docker / Docker Compose | 로컬 PostgreSQL 실행 | `infra/docker-compose.local.yml` |
| Java 21 | Backend 빌드/실행 | `backend/build.gradle`에 toolchain 21로 고정. 로컬에 21이 없으면 `./gradlew` 실행 시 자동 다운로드를 시도하나, 실패하면 직접 설치할 것 |
| Node.js | Frontend 빌드/실행 | 버전 pin(`.nvmrc`, `engines`) 없음 — 20 LTS 이상 권장, 문제 생기면 이 표에 버전을 명시해서 업데이트할 것 |

---

## 2. 최초 clone 후 실행 순서

```bash
git clone <repo-url>
cd bid-radar

# 1) DB 실행 (로컬 기본값: bidradar/bidradar/bidradar, 5433 포트)
docker compose -f infra/docker-compose.local.yml up -d

# 2) Backend 실행
cd backend
./gradlew bootRun
```

`./gradlew` 실행 시 `Permission denied`가 나면 실행권한이 없는 것이다 (PR#18에서 실제로 겪은 문제 — clone 방식이나 OS에 따라 실행권한 비트가 유실될 수 있다):

```bash
chmod +x backend/gradlew
```

```bash
# 3) Frontend 실행
cd frontend
npm install
npm run dev
```

Backend는 `application.yml`에 로컬 기본값이 다 채워져 있어서, 별도 `.env` 없이 1~2번만으로 기동된다.

---

## 3. 파트별 환경변수

### Backend

로컬 개발은 기본값으로 동작하므로 별도 설정이 필요 없다. 운영 배포나 실제 나라장터 API 연동이 필요하면 루트 `.env.prod.example`을 참고해 `.env`를 만든다.

- `G2B_SERVICE_KEY`: 공공데이터포털(data.go.kr)에서 "나라장터 입찰공고정보서비스" 활용신청 후 발급되는 **encoding 키**를 사용한다. 비워두면 앱은 정상 기동하지만 나라장터 API 호출은 실패한다.
- 나머지(`DB_*`, `JWT_SECRET`, `COOKIE_SECURE`)는 로컬 기본값으로 충분하다.

### Frontend

`frontend/.env.example`을 `.env.local`로 복사한다. 기본값(`VITE_API_BASE_URL=/api`)이면 대부분 그대로 쓴다.

### AI Worker

Epic-3 착수 전이라 아직 코드/의존성이 없다 (`ai-worker/`는 빈 디렉토리). Epic-3 착수 시 이 절을 채운다.

---

## 4. MCP / Codex 리뷰 연동 (PC마다 반복해야 하는 것)

- **Playwright MCP**: 저장소에 커밋된 `.mcp.json`이 `npx -y`로 자동 설치하므로 새 PC에서 별도 설정이 필요 없다.
- **Codex 리뷰 코멘트 확인용 read-only PAT**: Claude Code가 Codex 리뷰 결과를 읽으려면 PC마다 개인 토큰을 발급해 `BID_RADAR_GH_PR_READ_TOKEN` 환경변수로 등록해야 한다. 발급 절차와 권한 범위는 `docs/rules/codex-review-rule.md` 3~4절을 그대로 따른다. 토큰 값은 어떤 파일에도 기록하지 않는다.

---

## 5. 자주 겪는 함정

- `./gradlew: Permission denied` → `chmod +x backend/gradlew` (2절 참고)
- `docker compose` 포트 충돌(5433) → 다른 프로젝트의 postgres 컨테이너와 겹치는지 확인
- G2B API 호출이 계속 실패 → `G2B_SERVICE_KEY` 미설정 또는 잘못된 키(디코딩 키를 넣은 경우가 흔한 실수 — encoding 키를 써야 한다)
