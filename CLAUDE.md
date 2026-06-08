# Bid Radar

나라장터 입찰 공고를 수집하고, 회사 프로필 기준으로 적합도/리스크/체크리스트를 분석해 검토 가치 있는 공고를 선별하는 B2B SaaS.

---

## 기술 스택

| 파트 | 기술 |
|---|---|
| Backend | Spring Boot, Spring Scheduler, Spring Data JPA, PostgreSQL |
| AI Worker | Python, FastAPI, pdfplumber, hwp5, Claude/OpenAI API |
| Frontend | React, Vite |
| Infra | AWS EC2, RDS, S3, Docker, Docker Compose |
| CI/CD | GitHub Actions |

---

## 디렉토리 구조

```
bid-radar/
├── CLAUDE.md
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
| 커밋/푸시 시 | `docs/rules/git-rule.md` |
| Task 수행 시 | `docs/rules/task-rule.md` |
| 코드 완료 후 (공통 검토) | `docs/rules/self-review-rule.md` |
| Spring Boot 작업 시 | `docs/rules/springboot/` |
| Spring Boot 코드 완료 후 | `docs/rules/springboot/springboot-review-rule.md` |
| AI Worker 작업 시 | `docs/rules/ai-worker/ai-worker-rule.md` |
| AI Worker 코드 완료 후 | `docs/rules/ai-worker/ai-worker-review-rule.md` |
| Frontend 작업 시 | `docs/rules/frontend/frontend-rule.md` |
| Frontend 코드 완료 후 | `docs/rules/frontend/frontend-review-rule.md` |

---

## Epic 구성

| Epic | 완료 기준 |
|---|---|
| Epic-1 공고 탐색 | 사용자가 오늘 등록된 공고를 브라우저에서 조회할 수 있다 |
| Epic-2 적합도 판단 | 사용자가 회사 기준으로 적합한 공고를 선별할 수 있다 |
| Epic-3 AI 문서 분석 | 공고 상세 화면에서 AI 분석 결과를 확인할 수 있다 |
| Epic-4 운영 고도화 | 서비스를 실제 운영 가능한 수준으로 안정화한다 |
