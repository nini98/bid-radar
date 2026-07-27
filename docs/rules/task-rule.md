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
backend | frontend | ai-worker | infra | docs

## 관련 Issue (Issue에서 파생된 경우에만 작성, 아니면 섹션 자체를 생략)
- Issue: #{이슈번호}
- 담당 Next Step (생성 시점 원문 그대로 인용):
  - [ ] {Next Step 항목 원문 1}
  - [ ] {Next Step 항목 원문 2}

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

`파트` 값은 `docs/rules/issue-rule.md` §4 Part 라벨과 동일한 값을 쓴다 — Task와 Issue의 Part를 변환 없이 직접 비교하기 위함이다 (`docs/rules/issue-rule.md` §5 점검 절차에서 사용). 여러 파트에 걸치면 `+`로 연결한다 (예: `backend + frontend + infra`).

`## 관련 Issue` 섹션은 Task가 GitHub Issue에서 파생된 경우에만 작성한다. Task 생성 시점 기준의 출처와 범위를 남기는 참고용 스냅샷일 뿐이며, Issue 본문이 이후 수정되어도 자동으로 동기화되지 않는다. Task의 실행 범위와 완료 기준은 이 섹션이 아니라 해당 Task의 `Done Condition`을 기준으로 판단한다. Issue와 무관하게 시작하는 Task는 이 섹션을 생략한다.

담당 Next Step은 번호가 아니라 **항목 원문을 그대로 인용**해서 남긴다. Issue의 Next Step은 순서·번호가 고정되지 않으므로, 이후 항목이 삽입·삭제·재정렬되면 번호 범위는 원래 가리키던 항목을 더 이상 가리키지 않게 된다. 원문 인용은 이런 변경에도 내용으로 식별할 수 있다 (문구 자체가 수정되는 경우의 처리는 아래 §4 14번 참고).

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

### 4-0. Issue 기반 Task 분해 규칙

Issue에서 파생된 작업은 Task 파일을 만들기 전에 아래를 먼저 수행한다.

1. 작업 수행자가 해당 Issue를 몇 개의 Task(= PR 단위)로 나눌지 분해안을 보고한다.
2. 사용자 승인을 받은 뒤에만 Task 파일을 생성한다 (§2의 `## 관련 Issue` 섹션 포함).

Issue와 무관하게 시작하는 Task는 이 절차를 적용하지 않는다.

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
3. **이전부터 있던 별개 이슈**는 이번 PR에서 해결하지 않는다면 규모와 무관하게 GitHub Issue로 등록한다 (이번 PR에는 known limitation으로 명시):
   - 변경 규모가 작고(non-breaking) 별도 설계 합의 없이 판단 가능하면, 같은 PR에 포함해도 된다 (사용자 승인 후 PR 설명에 "범위 확장: {내용}" 명시). 이 경우 Issue 등록은 생략한다.
   - 신규 엔드포인트, 계약 의미 변경 등 규모가 크거나 설계 문서(Notion) 업데이트·별도 논의가 필요하면, 이번 PR에서 고치지 않고 Issue로 분리한다.
   - 어느 경우든 Issue로 분리할 때는 재현 조건·원인·관련 파일을 남기고, 이후 별도 Task 파일로 착수한다.

### 수행 중

5. `작업 항목`을 위에서 아래로 순서대로 수행한다.
6. 각 항목 완료 시 체크한다.
7. `Out of Scope` 항목을 침범하지 않는다.
8. 작업 중 `Done Condition` 외 변경이 필요하다고 판단되면 수행을 멈추고 사용자에게 확인한다.

### 완료 후

9. `Done Condition`을 모두 충족했는지 확인한다.
10. 리뷰를 수행한다.
    - `docs/rules/self-review-rule.md` (공통)
    - 파트별 Review Rule: backend → `docs/rules/springboot/springboot-review-rule.md` / ai-worker → `docs/rules/ai-worker/ai-worker-review-rule.md` / frontend → `docs/rules/frontend/frontend-review-rule.md` / infra, docs → 별도 파트별 Review Rule 없음 (`self-review-rule.md`로 충분)
11. Task 파일의 `Done Condition`과 `작업 항목`을 모두 `[x]`로 체크한다.
12. 커밋하고 원격 브랜치를 푸시한 뒤 PR을 생성한다.
13. Issue 기반 Task는 PR 본문에 `Refs #이슈번호`와 담당 Next Step 원문을 명시한다. 번호가 아니라 Task 파일의 `## 관련 Issue`에 스냅샷된 원문을 그대로 옮겨 적는다 (번호를 쓰지 않는 이유는 §2 설명 참고).

    ```
    Refs #19

    담당 Next Step:
    - [ ] Notion 문서 재확인
    - [ ] 백엔드 테스트 픽스처 정리
    ```

    하나의 Issue를 여러 Task/PR이 나눠 처리하는 경우 `Closes`/`Fixes`/`Resolves`는 쓰지 않고 모든 PR에 `Refs`만 사용한다 — 이 키워드들은 PR 머지 시 GitHub이 Issue를 자동으로 닫아버려서, 체크박스 갱신 전에 Issue가 닫혀 `docs/rules/issue-rule.md` §5의 "open issue만 스캔"하는 안전망 범위를 벗어나기 때문이다.
14. Issue 기반 Task의 PR이 머지되면, 아래 이중 트리거 중 해당하는 시점에 관련 Issue의 Next Step 체크박스를 갱신한다.
    - **트리거 A (즉시)**: 사용자가 머지 사실을 알리면, PR 머지 상태를 확인하고 담당 Next Step 체크박스를 갱신한다.
    - **트리거 B (안전망)**: 다음 브랜치 생성 전 점검(`docs/rules/issue-rule.md` §5)에서, open issue 중 `Refs #이슈번호`로 연결된 merged PR이 있는데 체크박스가 갱신되지 않은 경우를 확인해 복구한다. 이 처리는 지금 진행 중인 작업의 범위·순서에 영향을 주지 않는다.
    - 체크박스를 찾을 때는 순번이 아니라 Task의 `## 관련 Issue`에 스냅샷된 원문과 **내용이 일치하는 줄**을 Issue 본문에서 찾아 체크한다 (Next Step은 순서·번호가 고정되지 않아 삽입/재정렬되면 번호가 다른 항목을 가리킬 수 있음). 일치 판정은 체크박스 마커(`- [ ]`/`- [x]`)와 앞뒤 공백·들여쓰기를 제외한 텍스트 내용만 비교한다 — 들여쓰기나 공백 같은 서식 차이는 다른 항목으로 취급하지 않는다. 그 밖의 이유로(문구 자체가 수정·삭제되어) 일치하는 항목을 찾을 수 없으면 임의로 체크하지 않고 사용자에게 확인한다.
    - 체크박스를 갱신하는 즉시(트리거 A/B 어느 쪽이든), 해당 Issue의 모든 Next Step이 체크됐는지 함께 확인한다. 전부 체크됐다면 그 자리에서 바로 `gh issue close`로 Issue를 종료한다 — 종료 대상 PR은 이미 사용자가 머지를 승인한 것들이므로 별도 승인 없이 진행한다.

---

## 5. 금지 규칙

- 사용자가 구현 계획을 승인하기 전에 코드 작성, 커밋, 브랜치 생성을 시작하지 않는다 (오타 수정, 문서 수정, "계획 생략" 명시 시는 예외).
- Task 파일을 읽지 않고 작업을 시작하지 않는다.
- `참조 Rule`에 없는 Rule 파일을 임의로 로딩하지 않는다.
- `Done Condition`에 없는 것을 완료 기준으로 삼지 않는다.
- `Out of Scope`에 해당하는 작업을 임의로 수행하지 않는다.
- 설계 문서 없이 도메인 구조를 임의로 변경하지 않는다.
- Issue 기반 Task의 PR 본문에 `Closes`/`Fixes`/`Resolves` 키워드를 사용하지 않는다 (Issue 자동 close로 인한 안전망 이탈 방지, `Refs`만 사용).
