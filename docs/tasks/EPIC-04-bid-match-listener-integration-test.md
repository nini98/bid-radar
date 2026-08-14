# EPIC-04 BidMatchEventListener AFTER_COMMIT+@Async 실동작 통합 테스트

## 목표

`BidMatchEventListener`가 `@TransactionalEventListener(phase = AFTER_COMMIT)` + `@Async`로 의도한 대로(별도 스레드에서, 커밋 후에만, 롤백 시엔 실행되지 않고) 동작하는지 검증하는 통합 테스트를 추가한다.

---

## 파트

backend

---

## 관련 Issue

- Issue: #43

이슈 본문에 `## Next Step` 체크리스트가 없음 — "재현/백필 방법" 섹션에 기술된 통합 테스트 클래스 추가가 이슈 전체이며, 이 Task가 이슈 전체 범위를 담당한다.

---

## 참조 Rule

- `docs/rules/springboot/testing-rule.md`
- `docs/rules/springboot/transaction-rule.md` §12

---

## Done Condition

- [x] `BidMatchEventListenerIntegrationTest`가 아래 3가지를 검증한다
  - [x] 발행한 트랜잭션이 커밋되기 전에는 `MatchCalculationService.calculateAndSave()`가 호출되지 않고, 커밋 완료 후에는 결국 호출된다
  - [x] 호출이 테스트 메인 스레드가 아닌 `matchTaskExecutor`(스레드 이름 접두어 `match-exec-`)에서 실행된다
  - [x] 이벤트를 발행한 트랜잭션이 롤백되면 일정 시간이 지나도 `calculateAndSave()`가 호출되지 않는다
- [x] 전체 회사 목록에 의존하지 않고, 이 테스트가 만든 특정 `BidNotice`/`Company` 인자로 호출됐는지를 기준으로 검증한다 (공유 Testcontainers DB에 다른 테스트의 잔여 데이터가 있어도 흔들리지 않도록)

---

## Out of Scope

- `BidMatchEventListener`를 `TaskExecutor` 직접 주입 + `TaskRejectedException` 캐치 방식으로 바꾸는 것(프로덕션 코드 변경) → Issue #51에서 별도로 다룸. 이번 Task는 현재 구현(`@Async`)을 대상으로 한 테스트 추가만 한다
- `BidMatchResult`/실패 상태 표현 스키마 변경 → Issue #40에서 다룸

---

## 작업 항목

- [x] `backend/src/test/java/com/bidradar/match/event/BidMatchEventListenerIntegrationTest.java` 작성
  - [x] "커밋 후에만, 별도 스레드에서 호출된다" 테스트
  - [x] "트랜잭션 롤백 시 호출되지 않는다" 테스트
- [x] self-review + `springboot-review-rule.md` 점검
