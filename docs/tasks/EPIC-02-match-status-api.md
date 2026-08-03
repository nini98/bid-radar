# EPIC-02 매칭 계산 상태 조회/재시도 API

## 목표

회사별 매칭 계산 상태(`IN_PROGRESS`/`DONE`/`FAILED`/미시작)를 조회하는 API와, `FAILED` 상태일 때 프로필 재전송 없이 재계산만 다시 트리거하는 재시도 API를 추가한다.

---

## 파트

backend

---

## 관련 Issue

- Issue: #33

담당 Next Step (생성 시점 원문 그대로 인용, Issue 본문과 동일하게 들여쓰기 없이 기록):
- [x] 상태 조회 전용 엔드포인트 및 `FAILED` 재시도 전용 엔드포인트 추가

---

## 참조 Rule

- `docs/rules/springboot/architecture-rule.md`
- `docs/rules/springboot/api-response-rule.md`
- `docs/rules/springboot/dto-response-rule.md`
- `docs/rules/springboot/exception-rule.md`
- `docs/rules/springboot/transaction-rule.md`
- `docs/rules/springboot/testing-rule.md`

---

## Done Condition

- [x] `GET /api/companies/me/match-status`가 현재 인증된 사용자 회사의 계산 상태(`IN_PROGRESS`/`DONE`/`FAILED`, 이력 없으면 상태값 없음)를 반환한다
- [x] `POST /api/companies/me/match-status/retry`가 상태가 정확히 `FAILED`일 때만 락을 재획득하고 재계산을 다시 트리거한다
- [x] 상태가 `FAILED`가 아닌데 재시도를 호출하면 전용 에러 코드로 거부된다
- [x] 회사 프로필이 없는 사용자가 두 엔드포인트를 호출하면 404로 응답한다
- [x] 재계산 트리거 이벤트가 "프로필 저장" 전용 이름(`CompanyProfileSavedEvent`)에서 재계산 신호 자체를 나타내는 이름(`MatchRecalculationRequestedEvent`)으로 분리되어, 프로필 저장 경로와 재시도 경로 양쪽에서 재사용된다
- [x] Notion `03. API 명세`에 신규 엔드포인트 2개가 반영된다 (§5 상세 설명, §10 API 목록 요약 표)

---

## Out of Scope

- 프론트엔드 폴링/캐시 무효화/Toast 처리 → Task 3 (이번 Task 완료 후 착수)
- 개별 공고 단위 계산 실패 상태 표현 (Issue #40) → 별도 Task, 이번 Task와 엔티티가 다름 (`BidMatchResult` vs `MatchCalculationStatus`)
- `MatchCalculationStatusType`에 4번째 상태값(`NOT_STARTED` 등) 추가 → 기존 관례(행 부재 = 미시작) 유지

---

## 작업 항목

- [x] `MatchRecalculationRequestedEvent` 신설 (`match.event` 패키지), 기존 `CompanyProfileSavedEvent` 삭제
- [x] `CompanyProfileService.saveProfile()`이 새 이벤트를 발행하도록 수정
- [x] `CompanyProfileMatchEventListener`가 새 이벤트를 구독하도록 수정
- [x] `MatchCalculationStatusResponse` DTO 추가
- [x] `MatchCalculationStatusService` 추가: 상태 조회, 재시도(락 재획득 + 이벤트 발행) 로직
- [x] `MatchCalculationStatusController` 추가: `GET /api/companies/me/match-status`, `POST /api/companies/me/match-status/retry`
- [x] `ResultCode`에 재시도 거부 전용 코드 추가
- [x] 기존 이벤트/리스너 관련 테스트 클래스명·참조 갱신
- [x] 신규 Service/Controller 단위 테스트 작성
- [x] Notion `03. API 명세` 갱신
