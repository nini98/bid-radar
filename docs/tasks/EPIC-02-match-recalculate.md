# EPIC-02 매칭 계산 트리거 (자동 계산 + 재계산 API)

## 목표

신규 공고 수집 시 전체 회사에 대해 매칭 점수를 자동 계산하고, 회사 프로필 변경 시 `POST /api/companies/me/recalculate`로 비동기 재계산을 요청할 수 있게 한다. 매칭 계산은 공고 수집 트랜잭션과 분리한다.

---

## 파트

backend

---

## 참조 Rule

- `docs/rules/springboot/architecture-rule.md`
- `docs/rules/springboot/transaction-rule.md`
- `docs/rules/springboot/api-response-rule.md`
- `docs/rules/springboot/logging-rule.md`

---

## Done Condition

- [x] 신규 공고가 `bid_notices`에 커밋된 이후, 전체 회사에 대해 `bid_match_results`가 자동 생성된다 (best-effort — `ApplicationEventPublisher` + `@Async`는 전달을 영속화하지 않아, 커밋 후 리스너 실행 전 프로세스 종료나 스레드풀 큐 포화 시 유실될 수 있다. 유실돼도 해당 회사가 이후 `RecalculateService`로 재계산하면 정정됨. Codex 리뷰, PR #42 참고)
- [x] 매칭 계산은 공고 저장 트랜잭션과 분리되어 실행된다 (매칭 계산 실패가 공고 저장에 영향을 주지 않는다)
- [x] `POST /api/companies/me/recalculate` 요청 시 즉시 응답이 반환되고, 실제 재계산은 비동기(`@Async`)로 수행된다
- [x] 재계산은 AI 분석을 재실행하지 않고 `bid_match_results`만 갱신한다
- [x] 특정 회사/공고의 재계산 중 예외가 발생해도 나머지 대상의 재계산은 계속 진행된다 (에러 로그 기록)

---

## Out of Scope

- 매칭 계산 진행 상태(진행중/완료) 추적 필드나 폴링 API → 프론트는 재조회로 확인, 상태 테이블은 Epic-4 이후 검토
- Rule Engine 점수 계산 로직 자체 → `EPIC-02-match-rule-engine`
- `collection_jobs`/`analysis_jobs` 이력 관리 → Epic-4

---

## 작업 항목

- [x] `BidNoticeCollectedEvent` 정의 (bidNoticeId)
- [x] `BidNoticeProcessor`에서 공고 저장 후 `ApplicationEventPublisher`로 이벤트 발행 (`BidCollectorService`는 저장 트랜잭션을 갖지 않아, 실제 저장 트랜잭션을 보유한 `BidNoticeProcessor.process()`에서 발행 — 사용자 확인 후 결정)
- [x] `BidMatchEventListener` 구현 — `@TransactionalEventListener(phase = AFTER_COMMIT)`으로 이벤트 수신, 전체 회사 목록을 조회해 회사별로 `MatchCalculationService` 호출
- [x] 비동기 실행 설정 — `@EnableAsync` 활성화, 전용 `ThreadPoolTaskExecutor` 빈 등록
- [x] 이벤트 리스너와 재계산 로직에 `@Async` 적용, 회사/공고 단위로 예외를 격리해 하나 실패해도 나머지가 계속 진행되도록 처리 + 실패 시 에러 로그 기록
- [x] `RecalculateService` 구현 — 로그인한 사용자의 회사를 기준으로 전체 공고에 대해 `bid_match_results`를 재계산 (`@Async`)
- [x] `CompanyProfileController`에 `POST /api/companies/me/recalculate` 엔드포인트 추가 — 즉시 공통 Wrapper 응답 반환 후 비동기 재계산 트리거 (회사 프로필 없으면 동기적으로 404 — 사용자 확인 후 결정)
