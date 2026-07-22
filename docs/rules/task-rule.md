# Task Rule

## 1. 문서 목적

Task 수행 방식과 Task 파일 구조를 정의한다.

---

## 2. Task 파일 구조

모든 Task는 `docs/tasks/EPIC-{번호}-{설명}.md` 형식으로 작성한다.

```markdown
# EPIC-{번호} {Task 이름}

## 목표
한 줄로 무엇을 만드는지 기술한다.

## 파트
Spring Boot | AI Worker | Frontend

## 참조 Rule
- docs/rules/springboot/entity-rule.md
- docs/rules/springboot/repository-rule.md

## Done Condition
- [ ] 완료로 판단하는 구체적인 조건 1
- [ ] 완료로 판단하는 구체적인 조건 2

## Out of Scope
- 이번 Task에서 하지 않을 것들

## 작업 항목
- [ ] 세부 작업 1
- [ ] 세부 작업 2
```

---

## 3. 참조 Rule 선택 기준 (Spring Boot)

Task 파일 작성 시 작업 내용에 맞는 Rule만 선택한다.

| 작업 내용 | 읽을 Rule |
|---|---|
| 엔티티 설계/수정 | architecture-rule, entity-rule |
| Repository 작성 | repository-rule |
| Service 작성 | transaction-rule |
| API 엔드포인트 추가 | architecture-rule, api-response-rule, dto-request-rule, dto-response-rule, exception-rule |
| DTO/변환 작성 | mapper-rule, dto-request-rule, dto-response-rule |
| 보안/인증 구현 | security-rule |
| DB 스키마 변경 | flyway-db-migration-rule |
| 환경 설정 | config-property-rule |
| 테스트 작성 | testing-rule |
| 로깅 추가 | logging-rule |

---

## 4. Task 수행 루프

### 시작 전

1. Task 파일을 읽는다.
2. `Done Condition`과 `Out of Scope`를 먼저 확인한다.
3. `참조 Rule`에 명시된 파일만 로딩한다.
4. 코드를 작성하기 전에 구현 계획을 먼저 보고하고 사용자 승인을 받는다 (아래 4-1 참고). 승인 전에는 코드 작성, 커밋, 브랜치 생성을 시작하지 않는다.

### 4-1. 태스크 착수 규칙 (Plan-first)

- 구현 계획에는 다음을 포함한다.
  1. 변경/생성할 파일 목록
  2. 접근 방식 요약 (어떤 구조로 구현할지 3~5줄)
  3. 불확실한 지점: Task 문서에 명시되지 않아서 가정이 필요한 부분. 가정하지 말고 질문으로 남길 것
  4. 작성할 테스트 목록: 이 태스크의 핵심 동작을 검증하는 테스트를 요구사항 단위로 나열 (예: "중복 공고는 저장되지 않는다"). Spring Boot는 `docs/rules/springboot/testing-rule.md`, Frontend는 `docs/rules/frontend/testing-rule.md`의 기준에 따라 대상 여부와 (Spring Boot의 경우) 단위/통합 구분을 표시. 테스트 생략 허용 기준(Spring Boot는 `self-review-rule.md`의 단순 CRUD/설정/Mapper, Frontend는 `frontend/testing-rule.md` §4-2)에 해당하면 "테스트 생략: {사유}"로 명시하고, 생략한 화면 흐름은 Frontend Task에 한해 `docs/qa/` 시나리오 문서 작성 여부도 함께 표시.
- 개발 도중 계획에 없던 파일을 수정해야 하거나 접근 방식을 바꿔야 하는 상황이 생기면, 진행을 멈추고 사용자에게 보고 후 승인을 받는다.
- 예외 (계획 생략 가능): 오타 수정, 문서 수정, 사용자가 "계획 생략"이라고 명시한 경우.

### 4-2. 범위 밖 변경 발견 시 처리 기준

Task 수행 중이거나 Codex 리뷰 대응 중, `Done Condition`/PR 범위를 벗어나는 변경이 필요하다고 판단되면 아래 기준으로 처리 방식을 정하고 사용자에게 보고해 확인받는다.

1. **회귀 여부 판단**: 이번 Task/PR의 변경 자체가 만들어낸 결함인가, 아니면 이전부터 있던 별개 이슈가 드러난 것인가.
2. **이번 Task/PR이 직접 만든 회귀는 규모와 무관하게 Issue로 분리하지 않는다.** 문제가 남아있는 채로 known limitation 처리하고 머지하는 것을 금지하며, 아래 둘 중 하나로만 처리한다 (사용자 승인 필요):
   - 변경 규모가 작고(필드 추가 등 non-breaking, 기존 계약을 깨지 않음) 별도 설계 합의 없이 판단 가능하면 → **같은 PR에 포함**한다 (PR 설명에 "범위 확장: {내용}" 명시).
   - 수정 범위가 크거나(신규 엔드포인트, 계약 의미 변경 등) 설계 논의가 필요하면 → **회귀를 유발한 변경 자체를 이번 PR에서 제외**하고 범위를 좁혀 완료한다. 제외한 변경은 별도 Task로 재설계해 착수한다.
3. **이전부터 있던 별개 이슈**는 다음 중 하나라도 해당하면 GitHub Issue로 분리한다 (이번 PR에는 known limitation으로 명시하고 원래 범위만 완료):
   - 신규 엔드포인트, 계약 의미 변경 등 규모가 크다.
   - 설계 문서(Notion) 업데이트나 별도 논의가 필요하다.
   - 이슈에는 재현 조건·원인·관련 파일을 남기고, 이후 별도 Task 파일로 착수한다.

### 수행 중

5. `작업 항목`을 위에서 아래로 순서대로 수행한다.
6. 각 항목 완료 시 체크한다.
7. `Out of Scope` 항목을 침범하지 않는다.
8. 작업 중 `Done Condition` 외 변경이 필요하다고 판단되면 수행을 멈추고 사용자에게 확인한다.

### 완료 후

9. `Done Condition`을 모두 충족했는지 확인한다.
10. `docs/rules/self-review-rule.md`를 수행한다.
11. 파트별 Review Rule을 수행한다.
12. Task 파일의 `Done Condition`과 `작업 항목`을 모두 `[x]`로 체크한다.
13. 커밋한다.
    - Spring Boot → `docs/rules/springboot/springboot-review-rule.md`
    - AI Worker → `docs/rules/ai-worker/ai-worker-review-rule.md`
    - Frontend → `docs/rules/frontend/frontend-review-rule.md`

---

## 5. 금지 규칙

- 사용자가 구현 계획을 승인하기 전에 코드 작성, 커밋, 브랜치 생성을 시작하지 않는다 (오타 수정, 문서 수정, "계획 생략" 명시 시는 예외).
- Task 파일을 읽지 않고 작업을 시작하지 않는다.
- `참조 Rule`에 없는 Rule 파일을 임의로 로딩하지 않는다.
- `Done Condition`에 없는 것을 완료 기준으로 삼지 않는다.
- `Out of Scope`에 해당하는 작업을 임의로 수행하지 않는다.
- 설계 문서 없이 도메인 구조를 임의로 변경하지 않는다.
