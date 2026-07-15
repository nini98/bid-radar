# Bid Radar

나라장터 입찰 공고를 수집하고, 회사 프로필 기준으로 적합도/리스크/체크리스트를 분석해 검토 가치 있는 공고를 선별하는 B2B SaaS.

---

## 역할

이 저장소는 Claude Code와 Codex를 함께 사용한다.

- Claude Code의 기본 역할은 **구현자**이다. `docs/tasks/`의 태스크 단위로 기능을 구현하고, 완료 후 `docs/rules/self-review-rule.md`와 파트별 Review Rule로 자체 점검한다.
- Codex의 기본 역할은 **독립 리뷰어**이다. 리뷰 기준과 절차는 `AGENTS.md`에 정의되어 있다.
- Claude Code의 자체 점검은 Codex의 최종 리뷰를 대체하지 않는다.
- Codex 리뷰에서 findings가 나오면, Claude Code는 같은 브랜치에 fix 커밋을 추가하고 재리뷰를 요청한다.
- 승인된 Task나 설계 문서에 명시되지 않은 설계 변경, API 계약 변경, DB 스키마 변경처럼 영향 범위가 큰 작업은 두 에이전트 모두 사용자 확인 후 진행한다.

---

## 기술 스택

| 파트 | 기술 |
|---|---|
| Backend | Java 21, Spring Boot 3.x, Spring Scheduler, Spring Data JPA, QueryDSL, PostgreSQL, Flyway |
| AI Worker | Python, FastAPI, pdfplumber, hwp5, Claude/OpenAI API |
| Frontend | React, Vite |
| Infra | AWS EC2, RDS, S3, Docker, Docker Compose |
| CI/CD | GitHub Actions |

---

## 디렉토리 구조

```
bid-radar/
├── CLAUDE.md
├── AGENTS.md
├── backend/
├── frontend/
├── ai-worker/
├── infra/
└── docs/
    ├── rules/
    │   ├── task-rule.md
    │   ├── self-review-rule.md
    │   ├── springboot/
    │   ├── ai-worker/
    │   └── frontend/
    └── tasks/
```

---

## 절대 금지

1. **AI Worker는 DB에 직접 접근하지 않는다** — 분석 결과만 콜백으로 반환한다
2. **AI가 적합도 점수를 계산하지 않는다** — 점수 계산은 Spring Boot Rule Engine이 담당한다
3. **AI Worker는 상태 변경, 저장, 비즈니스 로직을 수행하지 않는다**
4. **모든 API 응답은 공통 Wrapper를 사용한다** — `{ header: { resultCode, resultMessage }, data }`
5. **설계 문서 없이 도메인 구조를 임의로 변경하지 않는다**
6. **main 브랜치에 `git push --force`를 실행하지 않는다**
7. **민감정보를 커밋하지 않는다** — API 키, 실제 비밀번호, 토큰, G2B 서비스 키, 운영 DB 접속 정보, 비밀 `.env` 값

---

## 설계 문서 참조 (Notion)

> 도메인 설계, 화면 기획, API 명세 등 상세 설계는 Notion을 Source of Truth로 사용한다.
> 작업 전 반드시 해당 문서를 읽고 시작한다.

| 문서 | 내용 | Notion |
|---|---|---|
| 프로젝트 개요 | 목표, 기능, 시스템 구조 | https://app.notion.com/p/35d283f8e5ab80c48d56e2a0fa6c45ea |
| 01. 화면기획 | 화면 목록, 와이어프레임, 화면 흐름 | https://app.notion.com/p/35e283f8e5ab806392a2cfdbeac058be |
| 02. 도메인/DB 설계 | 테이블 설계, 도메인 관계, 인덱스 | https://app.notion.com/p/360283f8e5ab80ff8a78cacfb6ce9488 |
| 03. API 명세 | 엔드포인트, 인증 구조, 응답 형식 | https://app.notion.com/p/361283f8e5ab80feadd8d3df249ec174 |
| 04. 적합도 판단 엔진 | Rule Engine 구조, 점수 계산 로직 | https://app.notion.com/p/361283f8e5ab8016a5c7d0844050359f |
| 05. AI Worker 설계 | 분석 흐름, Chunk 전략, 콜백 구조 | https://app.notion.com/p/361283f8e5ab806f85dfdb7c7cb08b12 |
| 06. AI Agent 개발 규칙 | Rule 체계, 개발 프로세스 | https://app.notion.com/p/36f283f8e5ab800b986fe780ea6e3000 |

---

## Rule 참조

| 작업 | 읽을 Rule |
|---|---|
| 새 PC에서 개발 환경 셋업 시 | `docs/dev-setup.md` |
| 커밋/푸시 시 | `docs/rules/git-rule.md` |
| Task 수행 시 | `docs/rules/task-rule.md` |
| 코드 완료 후 (공통 검토) | `docs/rules/self-review-rule.md` |
| Spring Boot 작업 시 | `docs/rules/springboot/` |
| Spring Boot 코드 완료 후 | `docs/rules/springboot/springboot-review-rule.md` |
| AI Worker 작업 시 | `docs/rules/ai-worker/ai-worker-rule.md` |
| AI Worker 코드 완료 후 | `docs/rules/ai-worker/ai-worker-review-rule.md` |
| Frontend 작업 시 | `docs/rules/frontend/frontend-rule.md` |
| Frontend 코드 완료 후 | `docs/rules/frontend/frontend-review-rule.md` |
| PR push 후 Codex 리뷰 확인 시 | `docs/rules/codex-review-rule.md` |

---

## Git / PR 워크플로우 규칙

### 브랜치 규칙

- 브랜치는 태스크 착수 시점에 생성한다. 미리 만들어두지 않는다 (사이클 타임 측정 기준점이기 때문).
- 브랜치 네이밍:
  - `feature/{epic}-{태스크명}` — 신규 기능 개발
  - `fix/{내용}` — 일반 버그 수정 (급하지 않음, 정상 PR 절차)
  - `hotfix/{내용}` — 운영 중인 서비스의 심각한 버그를 신속히 배포해야 하는 경우
  - `chore/{내용}` — 설정, 의존성 등 (상세는 `docs/rules/git-rule.md` 참고)
  - `fix`와 `hotfix`의 구분은 "머지 여부"가 아니라 **긴급도**다. 정상 릴리즈 사이클로 처리 가능하면 `fix`, 즉시 배포가 필요하면 `hotfix`.
- `main`에 직접 커밋/푸시 금지. 모든 변경은 PR을 경유한다.

### 태스크 완료 시 절차

1. 변경사항 커밋
2. 브랜치 원격 푸시
3. `gh pr create`로 PR 생성 (base: `main`)

### PR 작성 규칙

- 제목: `[{epic}] {태스크 요약}`
- 본문에 "변경 내용 / 이유 / 확인 방법" 3개 섹션 필수

### 절대 금지

- PR 머지 금지: `gh pr merge` 실행 금지. 머지는 사용자만 한다.
- 푸시된 커밋에 `git reset`, force push 금지 (자신만 작업 중인 브랜치라도 예외 없음). 되돌림은 revert 커밋으로.
  - AI 에이전트는 브랜치를 다른 사람이 이미 pull했는지, 리뷰가 진행 중인지 확실히 알 수 없기 때문에 일반적인 rebase 후 force-push 관행보다 보수적으로 간다.
- 리뷰 중 발견된 문제로 새 fix 브랜치 생성 금지. 같은 브랜치에 수정 커밋 추가.

### 문제 상황 처리

- **리뷰 수정사항**: 같은 브랜치에 커밋+푸시 (기존 PR에 자동 반영). PR 닫거나 새로 만들지 않음.
- **머지 충돌**: 작업 브랜치에서 `git merge main`으로 해결한다 (rebase 아님 — force-push 금지 규칙과 일관되게, 커밋 해시를 바꾸지 않는 방식만 사용). 충돌 해결 후 커밋 전에 어느 쪽 코드를 살렸는지 사용자에게 보고하고 확인받을 것.
- **머지 후 버그 발견**: 긴급하면 `hotfix/*`, 급하지 않으면 `fix/*` 브랜치 생성 후 동일한 PR 절차.
- **태스크 폐기**: 사용자 지시가 있을 때만 PR close, 사유를 PR 코멘트로 남김.

---

## Epic 구성

| Epic | 완료 기준 |
|---|---|
| Epic-1 공고 탐색 | 사용자가 오늘 등록된 공고를 브라우저에서 조회할 수 있다 |
| Epic-2 적합도 판단 | 사용자가 회사 기준으로 적합한 공고를 선별할 수 있다 |
| Epic-3 AI 문서 분석 | 공고 상세 화면에서 AI 분석 결과를 확인할 수 있다 |
| Epic-4 운영 고도화 | 서비스를 실제 운영 가능한 수준으로 안정화한다 |
