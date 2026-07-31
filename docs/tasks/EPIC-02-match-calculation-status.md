# EPIC-02 매칭 계산 상태 엔티티 + 이벤트 기반 트리거 전환

## 목표

회사별 매칭 계산 상태(`IN_PROGRESS`/`DONE`/`FAILED`)를 추적하는 엔티티를 도입하고, 프로필 저장 시 재계산 트리거를 별도 API 호출이 아닌 도메인 이벤트(`AFTER_COMMIT` + `@Async`) 방식으로 `saveProfile()`에 흡수하며, 계산 진행 중 저장 요청을 락으로 거부한다.

---

## 파트

backend

---

## 관련 Issue

- Issue: #33

담당 Next Step (생성 시점 원문 그대로 인용, Issue 본문과 동일하게 들여쓰기 없이 기록):
- [x] `POST /companies/me/recalculate` 제거, 재계산 트리거를 도메인 이벤트(`AFTER_COMMIT` + `@Async`) 방식으로 `saveProfile()`에 흡수 (Notion 03 API 명세 업데이트 포함)
- [x] `match` 도메인에 회사별 계산 상태 엔티티 추가, 락 유효기간(5분) 로직 포함 (Notion 02 DB 설계 업데이트 포함)
- [x] `saveProfile()`에 동시 저장 방지 락(진행 중이면 거부) 추가

---

## 참조 Rule

- `docs/rules/springboot/architecture-rule.md`
- `docs/rules/springboot/entity-rule.md`
- `docs/rules/springboot/transaction-rule.md`
- `docs/rules/springboot/api-response-rule.md`
- `docs/rules/springboot/exception-rule.md`
- `docs/rules/springboot/flyway-db-migration-rule.md`

---

## Done Condition

- [x] `match` 도메인에 회사별 계산 상태를 추적하는 신규 엔티티가 존재한다 (`company_id` 1:1, 상태값 `IN_PROGRESS`/`DONE`/`FAILED` 3종, "미시작"은 row 부재로 표현)
- [x] `POST /companies/me/recalculate` 엔드포인트가 제거된다
- [x] `CompanyProfileService.saveProfile()`이 저장 트랜잭션 커밋 후, 도메인 이벤트 발행 + `@TransactionalEventListener(phase = AFTER_COMMIT)` + `@Async` 조합으로 재계산을 트리거한다 (기존 `BidNoticeCollectedEvent`/`BidMatchEventListener` 패턴과 동일한 방식, 이벤트 타입은 재계산 전용으로 별도 정의 — 기존 이벤트 재사용 금지)
- [x] 재계산 시작 시 상태를 `IN_PROGRESS`로 기록하고, 배치(전체 공고 순회) 완료 시 `DONE`으로, 처리되지 않은 예외 발생 시 `FAILED`로 기록한다
- [x] 계산 상태가 이미 `IN_PROGRESS`이고 유효기간(5분) 이내면 `saveProfile()` 자체가 거부되고 명확한 에러 메시지로 응답한다 (`updated_at` 기준 5분 초과 시 죽은 작업으로 간주해 새 시도를 허용)
- [x] Notion `02. 도메인/DB 설계`에 신규 엔티티/테이블이 반영된다
- [x] Notion `03. API 명세`에서 `POST /companies/me/recalculate` 제거 및 `saveProfile()` 응답에 락 거부 케이스가 반영된다

---

## Out of Scope

- 상태 조회 전용 엔드포인트, `FAILED` 재시도 전용 엔드포인트 → `EPIC-02-match-status-api` (후속 Task)
- 프론트엔드 폴링/캐시 무효화/Toast 처리 → 후속 Task (Task 2 API 완료 후 착수)
- 개별 공고 단위 계산 실패 상태 표현 → Issue #40에서 별도로 다룸
- 범용 `job_type` 기반 다목적 job 테이블 → 채택하지 않음 (Rule of Three 미충족)

---

## 작업 항목

- [x] `match` 도메인에 계산 상태 엔티티 추가 (Flyway 마이그레이션 포함)
- [x] 재계산 전용 도메인 이벤트 클래스 정의
- [x] `CompanyProfileService.saveProfile()`에 동시 저장 방지 락 체크(상태 `IN_PROGRESS` + 5분 이내면 거부) 추가
- [x] `saveProfile()` 커밋 후 재계산 이벤트 발행
- [x] 재계산 이벤트 리스너 구현 (`AFTER_COMMIT` + `@Async`) — 상태를 `IN_PROGRESS` → `DONE`/`FAILED`로 갱신하며 기존 회사별 재계산 로직 수행
- [x] `CompanyProfileController`에서 `POST /companies/me/recalculate` 엔드포인트 제거, 관련 `RecalculateService` 호출부 정리
- [x] Notion `02. 도메인/DB 설계`, `03. API 명세` 갱신
