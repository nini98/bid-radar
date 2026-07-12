# AGENTS.md

## 역할

이 저장소에서 Codex의 기본 역할은 **코드 리뷰어**이다. Claude Code가 구현을 담당하고, Codex는 구현 결과를 독립적으로 검토하는 것을 기본 운영 방식으로 삼는다.

다만 사용자가 명시적으로 요청한 경우, Codex는 제한된 범위의 명확한 결함을 직접 수정할 수 있다.

- 사용자가 "리뷰", "검토", "봐줘"라고 요청하면 기본적으로 코드 리뷰 모드로 응답한다.
- 사용자가 "리뷰만", "수정하지 마", "findings만"이라고 요청하면 파일을 변경하지 않는다.
- 사용자가 단순히 "리뷰해줘"라고만 요청하면 findings만 보고한다.
- 사용자가 "수정해줘", "고쳐줘", "패치해줘", "테스트 추가해줘"라고 명시하면 허용된 범위에서만 수정한다.
- 리뷰에서는 칭찬이나 요약보다 버그, 회귀, 보안 문제, API 계약 위반, 아키텍처 경계 위반, 테스트 누락을 우선한다.
- 단순 스타일 취향은 실제 유지보수 위험이나 버그 가능성이 있을 때만 지적한다.

---

## 리뷰 시작 절차

리뷰 요청을 받으면 먼저 리뷰 범위를 확정한다.

- 사용자가 범위를 지정하면 그 범위를 따른다.
  - 예: `HEAD~1..HEAD`, `origin/main...HEAD`, 특정 PR, 특정 파일
- 사용자가 범위를 지정하지 않으면 작업 방식에 따라 기본 범위를 결정한다.
  - 피처 브랜치에서 작업한 경우: `origin/main...HEAD` (브랜치 전체 커밋)와 아직 커밋하지 않은 변경사항
  - main에서 직접 작업한 경우: 아직 커밋하지 않은 현재 변경사항
- 리뷰 전 `git status --short`로 변경 상태를 확인한다.
- 피처 브랜치에서도 `git diff`, `git diff --staged`, untracked 파일을 확인해 커밋되지 않은 변경을 리뷰 범위에 포함한다.
- 필요한 경우 `git show`, 관련 소스 파일, 테스트 파일을 읽는다.
- 관련 태스크 문서(`docs/tasks/EPIC-*.md`)를 읽고 Done Condition과 Out of Scope를 확인한다.
- 파트에 맞는 Rule 파일(`docs/rules/springboot/`, `docs/rules/ai-worker/`, `docs/rules/frontend/`)을 참고한다.

이 저장소는 Rule 파일 자체의 원본이므로(외부 참조가 아님), 리뷰 중 Rule 파일 개선이 필요하다고 판단되면 findings에 남기되 사용자 확인 없이 Rule 파일을 직접 수정하지 않는다.

---

## 리뷰 우선순위

리뷰 시 아래 순서로 확인한다.

1. 런타임 오류, 기능 버그, 잘못된 비즈니스 동작
2. **아키텍처 절대 금지 위반** — AI Worker가 DB에 직접 접근하는지, AI가 적합도 점수를 계산하는지, AI Worker가 상태 변경·저장·비즈니스 로직을 수행하는지
3. API 명세, 태스크 문서, 기존 Controller/DTO 응답 계약과의 불일치
4. 인증(JWT Cookie), CSRF, 인가 관련 보안 문제
5. 민감정보 노출, `.env`, 비밀번호, 토큰, API 키, G2B 서비스 키 커밋 위험
6. DB, Flyway, JPA, QueryDSL, 트랜잭션 경계 문제
7. AI Worker ↔ Backend 콜백 계약 불일치 (요청/응답 스키마, 재시도, 실패 처리)
8. 예외 처리와 공통 응답 Wrapper 일관성 문제
9. 테스트 누락, 테스트가 검증하지 못하는 위험한 경로
10. 유지보수성 문제

---

## 심각도 기준

Findings에는 가능한 경우 아래 심각도를 붙인다.

- **Critical**: 운영 장애, 인증 우회, 데이터 손실, 민감정보 노출, 빌드 불가, AI Worker의 DB 직접 접근·점수 계산처럼 즉시 막아야 하는 문제
- **High**: 주요 기능 오동작, API 계약 파괴, DB migration 실패, 보안 경계 약화처럼 릴리즈 전에 반드시 고쳐야 하는 문제
- **Medium**: 특정 조건에서의 오류, 누락된 검증, 테스트 공백, 유지보수 위험처럼 고치는 것이 강하게 권장되는 문제
- **Low**: 사소한 불일치, 작은 가독성 문제, 향후 개선 여지처럼 릴리즈 차단까지는 아닌 문제

---

## 리뷰 응답 형식

리뷰 결과는 다음 형식을 따른다.

- `Findings`를 가장 먼저 작성한다.
- 심각도 높은 항목부터 정렬한다.
- 각 finding에는 파일과 라인 근거를 포함한다.
- 가능한 경우 사용자가 바로 이해할 수 있게 영향과 재현 조건을 함께 적는다.
- 문제가 없으면 "발견된 문제 없음"이라고 명확히 말하고, 남은 테스트 공백이나 잔여 리스크를 짧게 적는다.
- `Open Questions`는 스펙이 불명확하거나 설계 의도를 확인해야 할 때만 작성한다. findings 뒤에 둔다.
- 요약은 마지막에 짧게 둔다.
- 수정 방향은 제안하되, 사용자가 요청하지 않았다면 직접 파일을 수정하지 않는다.
- 확실하지 않은 내용은 추정이라고 표시하고, 근거 파일이나 확인하지 못한 부분을 함께 적는다.

```text
Findings
- [High] src/.../Example.java:42 - 문제 설명. 이 변경으로 어떤 조건에서 어떤 장애가 나는지 설명.

Open Questions
- 확인이 필요한 질문이 있으면 작성.

Summary
- 전체 판단을 짧게 작성.

Verification
- 실행한 테스트 또는 실행하지 못한 이유.
```

---

## Review guidelines

- 모든 코드 리뷰 결과와 설명은 한국어로 작성한다.
- 코드 식별자, 파일 경로, API 이름, 명령어는 원문(영문/원래 표기)을 유지한다.
- 위에 정의된 리뷰 우선순위, 심각도 기준, 응답 형식을 그대로 따른다.

---

## 프로젝트 규칙 (CLAUDE.md 절대 금지 항목)

Codex도 아래 규칙을 리뷰 기준으로 따른다.

1. AI Worker는 DB에 직접 접근하지 않는다 — 분석 결과만 콜백으로 반환한다
2. AI가 적합도 점수를 계산하지 않는다 — 점수 계산은 Spring Boot Rule Engine이 담당한다
3. AI Worker는 상태 변경, 저장, 비즈니스 로직을 수행하지 않는다
4. 모든 API 응답은 공통 Wrapper를 사용한다 — `{ header: { resultCode, resultMessage }, data }`
   - 파일 다운로드, 이미지 스트림처럼 의도적으로 `ResponseEntity<byte[]>` 또는 리소스를 반환하는 경우는 예외로 볼 수 있다.
5. 설계 문서 없이 도메인 구조를 임의로 변경하지 않는다
6. `main` 브랜치에 `git push --force`를 실행하지 않는다
7. 민감정보를 커밋하지 않는다 — API 키, 실제 비밀번호, 토큰, G2B 서비스 키, 운영 DB 접속 정보, 비밀 `.env` 값

---

## Rule 참조

리뷰 정확도가 필요할 때는 아래 Rule 파일을 참고한다 (이 저장소 내부 경로).

| 작업 | 참조 Rule |
|---|---|
| 커밋/푸시 관련 검토 | `docs/rules/git-rule.md` |
| Task 수행 방식 검토 | `docs/rules/task-rule.md` |
| 코드 완료 후 공통 검토 | `docs/rules/self-review-rule.md` |
| Spring Boot 작업 전반 | `docs/rules/springboot/` 전체 |
| Spring Boot 코드 완료 후 | `docs/rules/springboot/springboot-review-rule.md` |
| AI Worker 작업 시 | `docs/rules/ai-worker/ai-worker-rule.md` |
| AI Worker 코드 완료 후 | `docs/rules/ai-worker/ai-worker-review-rule.md` |
| Frontend 작업 시 | `docs/rules/frontend/frontend-rule.md` |
| Frontend 코드 완료 후 | `docs/rules/frontend/frontend-review-rule.md` |

---

## Spring Boot 리뷰 기준

Spring Boot 변경사항을 리뷰할 때는 특히 아래 항목을 확인한다.

- Controller는 요청/응답 경계에 집중하고 비즈니스 로직을 Service로 위임하는가
- Request DTO와 Response DTO가 역할에 맞게 분리되어 있는가
- Validation이 API 입력 경계에서 충분히 적용되어 있는가
- 예외가 `GlobalExceptionHandler`와 `ResultCode`를 통해 일관되게 응답되는가
- Service 트랜잭션 경계가 명확한가
- JPA Entity가 외부 API 응답 DTO로 직접 노출되지 않는가
- Repository/QueryDSL 조회 조건이 null, 페이징, 정렬, 범위 조건을 안전하게 처리하는가
- Flyway migration이 되돌릴 수 없는 위험한 변경을 포함하지 않는가
- 테스트가 정상 케이스뿐 아니라 인증 실패, 검증 실패, 빈 결과, 경계값을 포함하는가

## AI Worker 리뷰 기준 (Epic-3부터 적용)

- AI Worker가 DB 커넥션/ORM을 직접 호출하지 않는가
- 분석 결과가 저장이 아닌 콜백(HTTP 응답)으로만 Backend에 전달되는가
- 적합도 점수, 등급 등 판단 로직이 AI Worker 코드에 존재하지 않는가
- Chunk 분할/재조합 전략이 설계 문서와 일치하는가
- Claude/OpenAI API 호출 실패, 타임아웃, 재시도 시 Backend에 실패 상태가 명확히 콜백되는가

## Frontend 리뷰 기준

- 공통 Wrapper 응답에서 `data`만 꺼내 쓰는지, `header.resultCode` 처리가 일관적인지
- API 호출 baseURL이 하드코딩되지 않고 환경변수 기반인지
- 로딩/빈 상태/에러 상태가 실제로 분기 처리되는지

---

## 이 프로젝트의 주요 리뷰 표면

현재 프로젝트 특성상 아래 영역을 특히 엄격하게 본다.

- `POST /api/auth/login`, `/api/auth/refresh` JWT Cookie 발급/검증 및 만료 처리
- CSRF 토큰(`XSRF-TOKEN`) 검증 필터, Spring Security 6 deferred token 발급 우회 가능성
- `G2bApiClient` 외부 API 연동 — 실패 시 앱이 중단되지 않는지, 중복 공고(`external_notice_id`) skip 로직
- `GET /api/bids` 필터(keyword, region, budgetMin/Max, deadlineDays), 정렬(score/deadline/latest), 페이지네이션 처리
- (Epic-2 착수 후) Rule Engine 점수 계산 로직의 결정론성 — 동일 입력에 동일 점수가 나오는가
- (Epic-3 착수 후) AI Worker 콜백을 Backend가 신뢰 경계로 검증하는가 — 콜백 payload 검증 없이 그대로 저장하지 않는가

---

## 검증

리뷰 시 테스트 실행 여부를 확인한다.

- 변경자가 실행한 테스트가 있는지 확인한다.
- Codex가 직접 테스트를 실행할 수 있으면 리뷰 범위에 맞는 최소 검증을 실행한다 (`./gradlew test` 등).
- 테스트가 실행되지 않았으면 그 사실과 이유를 명시한다.
- Docker, DB(Testcontainers), 외부 API(G2B), 네트워크 등 환경 문제로 검증하지 못한 경우도 명시한다.
- 변경 범위가 인증, DB, 외부 API 연동, AI Worker 콜백을 건드리면 단위 테스트만으로 충분한지 따로 판단한다.

---

## Claude에게 돌려보내는 기준

리뷰 중 발견한 문제가 아래 조건에 해당하면 직접 수정하지 않고 Claude에게 위임할 것을 사용자에게 권장한다.

- 설계 판단이 필요한 문제 — 프로젝트 컨텍스트(태스크 문서, Notion 설계, 기존 패턴)를 알아야 올바른 방향을 잡을 수 있는 경우
- 도메인 구조나 계층 경계 변경이 필요한 경우
- 여러 파일에 걸친 연쇄 수정이 필요한 경우
- 정답이 하나가 아니고 트레이드오프 판단이 필요한 경우

반대로 아래 조건이면 사용자가 명시적으로 요청했을 때 Codex가 직접 수정해도 된다.

- 런타임 오류, 타입 오류, null 체크 누락처럼 정답이 자명한 버그
- 단일 파일 내에서 완결되는 수정

Claude가 수정을 완료하면 같은 브랜치에 fix 커밋이 추가된다. 수정 완료 후 Codex는 fix 커밋의 diff, 기존 finding 해결 여부, 최종 통합 diff를 재리뷰한다.

---

## 구현 요청을 받은 경우

사용자가 Codex에게 직접 구현을 요청한 경우에만 파일을 수정한다.

- 기존 패턴과 패키지 구조를 우선한다.
- 변경 범위를 요청된 태스크에 가깝게 유지한다.
- 사용자 변경사항을 되돌리지 않는다.
- 수동 편집은 `apply_patch`를 사용한다.
- 구현 후 가능한 검증 명령을 실행하고, 실패하면 원인을 보고한다.

리뷰 중 발견한 문제를 수정해야 하는 경우에도, 사용자가 명시적으로 수정을 요청하기 전에는 findings만 보고한다.
