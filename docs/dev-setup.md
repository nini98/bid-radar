# 개발 환경 셋업

새 PC에서 이 저장소를 처음 세팅할 때 확인하는 문서다. 코드/규칙 문서가 아니라 로컬 환경 구성 절차만 다룬다.

---

## 1. 사전 요구 사항

| 도구 | 용도 | 비고 |
|---|---|---|
| Git | 저장소 clone | - |
| GitHub CLI (`gh`) | PR 생성/조회 | clone 후 `gh auth login` 필요 |
| Docker / Docker Compose | 로컬 PostgreSQL 실행 | `infra/docker-compose.local.yml` |
| Java 21 | Backend 빌드/실행 | `backend/build.gradle`에 toolchain 21로 고정. `backend/settings.gradle`에 foojay resolver 설정이 없어 자동 다운로드는 되지 않으므로 미리 직접 설치해야 한다 (없으면 `No locally installed toolchains match ...`로 실패) |
| Node.js | Frontend 빌드/실행 | `frontend/package-lock.json`의 `vite`/`@vitejs/plugin-react`가 `^20.19.0 \|\| >=22.12.0`을 요구함 (20.0~20.18은 엔진 요구사항 미충족). 저장소에 `.nvmrc`/`engines` pin은 아직 없으니 20.19+ 또는 22.12+ 사용할 것 |

---

## 2. 최초 clone 후 실행 순서

```bash
git clone <repo-url>
cd bid-radar

# 1) DB 실행 (로컬 기본값: bidradar/bidradar/bidradar, 5433 포트)
# --wait: healthcheck 통과할 때까지 대기 (없으면 Flyway가 DB 준비 전에 붙어 간헐적으로 실패할 수 있음)
docker compose -f infra/docker-compose.local.yml up -d --wait

# 2) Backend 실행 (별도 터미널에서 — bootRun은 포그라운드로 계속 떠 있는다)
cd backend
./gradlew bootRun
```

`./gradlew` 실행 시 `Permission denied`가 나면 실행권한이 없는 것이다 (PR#18에서 실제로 겪은 문제 — clone 방식이나 OS에 따라 실행권한 비트가 유실될 수 있다). 위 순서대로라면 이미 `backend/` 안에 있으므로 상대 경로로 복구한다:

```bash
chmod +x ./gradlew
```

Backend는 `bootRun`이 떠 있는 상태로 그대로 두고, **새 터미널을 열어** 아래를 진행한다. 새 터미널이 저장소 루트에서 열린다는 보장이 없으므로, 루트가 아니면 1절에서 clone한 경로로 먼저 이동한다.

```bash
# 새 터미널 — 저장소 루트가 아니면 먼저 이동 (예: cd ~/bid-radar)
cd frontend
cp .env.example .env.local
npm install
npm run dev
```

Backend는 `application.yml`에 로컬 기본값이 다 채워져 있어서, 별도 `.env` 없이 1~2번만으로 기동된다.

---

## 3. 파트별 환경변수

### Backend

로컬 개발은 기본값으로 동작하므로 별도 설정이 필요 없다.

- `G2B_SERVICE_KEY`: 공공데이터포털(data.go.kr)에서 "나라장터 입찰공고정보서비스" 활용신청 후 발급되는 **encoding 키**를 사용한다. 비워두면 앱은 정상 기동하지만 나라장터 API 호출은 실패한다.
- 나머지(`DB_*`, `JWT_SECRET`, `COOKIE_SECURE`)는 로컬 기본값으로 충분하다.

**로컬에서 실제 G2B 연동을 테스트하려면**: `./gradlew bootRun`은 루트 `.env` 파일을 자동으로 읽지 않는다(별도 dotenv 플러그인 없음). 파일을 만들지 말고 셸 환경변수로 직접 넘긴다.

```bash
G2B_SERVICE_KEY=발급받은키 ./gradlew bootRun
```

**운영 배포용 `.env.prod`**는 이것과 별개다. `.github/workflows/deploy.yml`이 EC2 서버의 `/app/.env.prod` 파일을 `--env-file`로 참조하므로, 루트 `.env.prod.example`은 그 서버 파일을 만들 때 참고하는 템플릿일 뿐이며 이 저장소의 로컬 실행과는 무관하다.

### Frontend

`frontend/.env.example`을 `.env.local`로 복사한다. 기본값(`VITE_API_BASE_URL=/api`)이면 대부분 그대로 쓴다.

### AI Worker

Epic-3 착수 전이라 아직 코드/의존성이 없다 (`ai-worker/`는 빈 디렉토리). Epic-3 착수 시 이 절을 채운다.

---

## 4. MCP / Codex 리뷰 연동 (PC마다 반복해야 하는 것)

- **Playwright MCP**: 저장소에 커밋된 `.mcp.json`이 `npx -y`로 MCP 서버 프로세스 자체는 자동 실행하지만, 실제 브라우저 바이너리는 별도 설치해야 한다. `.mcp.json`이 `--browser chromium`로 고정돼 있어 시스템에 설치된 Chrome이 아니라 Playwright가 관리하는 Chromium을 사용하기 때문이다. 새 PC마다 최초 1회:
  ```bash
  npx playwright@1.62.0-alpha-1783623505000 install chromium
  ```
  버전을 반드시 고정해서 설치해야 한다. 그냥 `npx playwright install chromium`을 쓰면 그 시점의 최신 `playwright` CLI가 받아지는데, 브라우저 바이너리는 패키지 버전별 revision에 묶여 있어서 `.mcp.json`에 고정된 `@playwright/mcp@0.0.78`이 기대하는 Chromium revision과 어긋날 수 있다 (MCP 실행 시 executable을 못 찾는 에러 발생). 위 버전은 `npm view @playwright/mcp@0.0.78 dependencies`로 확인한 `playwright-core` 버전이다 — `.mcp.json`의 MCP 패키지 버전을 올리면 이 값도 같은 방법으로 다시 확인해서 갱신할 것.

  WSL(Linux)은 이것만으로 부족하고 시스템 라이브러리도 추가로 설치해야 한다 — 6절 참고. macOS는 아직 실측하지 않았다 — 7절 참고.
- **Notion MCP**: `.mcp.json`에 커밋돼 있지 않다. 로컬 스코프(`-s local`, 이 PC + 이 사용자 계정 전용)로 등록돼 있어서 새 PC에서는 매번 다시 등록해야 한다.
  ```bash
  claude mcp add --transport http notion https://mcp.notion.com/mcp -s local
  ```
  등록 후 Claude Code 세션에서 Notion MCP 도구를 처음 호출하면 브라우저로 Notion 로그인(OAuth) 창이 뜬다. 로그인하면 그 PC에서는 이후 세션마다 다시 로그인할 필요 없다.
- **Codex 리뷰 코멘트 확인용 read-only PAT**: Claude Code가 Codex 리뷰 결과를 읽으려면 PC마다 개인 토큰을 발급해 `BID_RADAR_GH_PR_READ_TOKEN` 환경변수로 등록해야 한다. 발급 절차와 권한 범위는 `docs/rules/codex-review-rule.md` 3~4절을 그대로 따른다. 토큰 값은 어떤 파일에도 기록하지 않는다.

---

## 5. 자주 겪는 함정

- `./gradlew: Permission denied` → `backend/` 안에서 `chmod +x ./gradlew` (2절 참고)
- `docker compose` 포트 충돌(5433) → 다른 프로젝트의 postgres 컨테이너와 겹치는지 확인
- G2B API 호출이 계속 실패 → `G2B_SERVICE_KEY` 미설정 또는 잘못된 키(디코딩 키를 넣은 경우가 흔한 실수 — encoding 키를 써야 한다)

---

## 6. WSL 참고

Windows에서 WSL로 작업하는 경우 macOS/Linux와 명령어 자체는 동일하지만(POSIX 셸 기준), 아래 두 가지는 WSL에만 해당하는 함정이라 추가로 확인한다.

- **Docker Desktop WSL2 통합**: Docker Desktop을 설치하는 것만으로는 WSL 안에서 `docker compose`가 데몬에 연결되지 않는다. Docker Desktop → Settings → Resources → WSL Integration에서 사용 중인 배포판(distro)에 통합을 켜야 한다.
- **저장소는 WSL 파일시스템에 clone한다**: `/mnt/c/Users/...`(윈도우 드라이브)에 clone하면 파일 I/O가 크게 느려지고, `chmod +x ./gradlew`로 준 실행권한이 NTFS 쪽에서 제대로 유지되지 않는 경우가 있어 2절의 `Permission denied` 문제가 반복될 수 있다. `~/projects/bid-radar`처럼 WSL 안의 Linux 파일시스템에 clone할 것.
- **Playwright MCP 브라우저 실행에 필요한 시스템 라이브러리**: WSL은 `libatk` 등 GUI 관련 라이브러리가 기본 설치돼 있지 않아, 4절의 브라우저 바이너리 설치만으로는 브라우저가 뜨지 않는다(`Host system is missing dependencies` 류 에러). 아래를 추가로 실행한다.
  ```bash
  sudo npx playwright install-deps chromium
  ```

---

## 7. macOS 참고

**미검증.** `.mcp.json`의 Playwright MCP 설정 자체는 원래 macOS에서 먼저 작성되어 동작한 것이지만(Git 히스토리상 최초 커밋이 macOS 작업 중 추가됨), 그 때 브라우저 바이너리 설치나 시스템 의존성 설치 같은 별도 조치가 필요했는지는 기록이 남아있지 않아 확실치 않다. WSL 절(6절)처럼 macOS에만 해당하는 함정이 있는지, 4절의 버전 고정 설치 명령만으로 충분한지 등은 다음에 macOS에서 세팅할 때 실제로 확인하고 이 절을 채울 것.

---

## 8. AWS / Terraform 셋업 (PC마다 반복해야 하는 것)

`infra/terraform/`의 코드와 AWS에 떠 있는 실제 리소스는 clone/로그인과 무관하게 그대로 유지된다. 하지만 그걸 다루는 **로컬 도구(CLI)와 자격증명은 PC마다 새로 설정**해야 한다 (2026-07-19, PR #31 작업 중 실측).

### 8-1. 필요한 CLI 설치

| 도구 | macOS | WSL/Linux |
|---|---|---|
| AWS CLI v2 | `brew install awscli` | 공식 설치 스크립트 (`https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip`) 또는 `sudo apt install awscli` |
| Terraform | `brew install terraform` | [HashiCorp 공식 apt 저장소](https://developer.hashicorp.com/terraform/install) 또는 `releases.hashicorp.com`에서 바이너리 직접 다운로드 |

### 8-2. AWS 자격증명 프로파일 2개 등록

IAM 사용자 자체는 AWS 계정에 이미 있으므로(어느 PC에서든 접근 가능), **각 PC에서는 그 사용자의 Access Key만 새로 발급받아 로컬에 등록**하면 된다. 기존 PC에서 쓰던 키 값을 그대로 옮겨써도 되지만, 분실 시 개별 무효화가 쉽도록 PC(기기)별로 새 키를 발급하는 걸 권장한다.

1. **`bid-radar-viewonly`** — ViewOnlyAccess 권한. Claude Code 세션이 AWS 상태를 조회할 때 이 프로파일만 사용한다 (생성/삭제 불가).
2. **관리자 권한 프로파일** (로컬에서는 `jay-admin`으로 명명) — 실제로 `terraform apply`, `aws ec2 stop-instances` 등 변경/삭제를 실행할 때 사용자가 직접 쓰는 프로파일. AdministratorAccess 권한.

두 프로파일 다 IAM 콘솔에서 Access Key 발급 후:

```bash
aws configure --profile bid-radar-viewonly
aws configure --profile jay-admin   # 프로파일 이름은 자유, 관리자 계정임을 알 수 있게 명명
```

Region은 `ap-northeast-2`, Output format은 `json`으로 설정한다. 자격증명은 절대 커밋하지 않는다 (`~/.aws/credentials`는 저장소 밖에 위치).

**권한 분리 원칙**(자세한 배경은 `docs/bid-radar-aws-infra-summary.md`와 PR #31 참고): 조회는 항상 ViewOnly로, 실제 리소스 변경은 항상 관리자 프로파일로 **사용자가 직접** 실행한다. Claude Code 세션에는 관리자 자격증명을 연결하지 않는다.

### 8-3. `infra/terraform/` 최초 실행

```bash
cd infra/terraform
AWS_PROFILE=bid-radar-viewonly terraform init
AWS_PROFILE=bid-radar-viewonly terraform plan   # 실제 상태와 코드가 일치하는지 확인용 (읽기 전용)
```

리소스를 실제로 만들거나 지우는 `terraform apply`는 `AWS_PROFILE=jay-admin`(또는 새로 등록한 관리자 프로파일명)으로 실행한다.
