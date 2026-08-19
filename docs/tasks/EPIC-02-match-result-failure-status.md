# EPIC-02 공고별 매칭 계산 실패 상태 기록

## 목표

`BidMatchResult`(공고 × 회사 단위 매칭 결과)에 실패 상태를 추가해, 개별 계산 실패가 로그에만 남지 않고 조회 가능한 상태로 남도록 한다.

## 파트

backend

## 관련 Issue

- Issue: #40

담당 Next Step (생성 시점 원문 그대로 인용):
- [x] `BidMatchResult` 실패 상태 스키마 설계 및 Notion 02번 문서 업데이트
- [x] `RecalculateService`/`MatchCalculationService`에서 개별 실패 시 실패 상태로 저장하도록 수정

## 참조 Rule

- `docs/rules/springboot/architecture-rule.md`
- `docs/rules/springboot/entity-rule.md`
- `docs/rules/springboot/transaction-rule.md`
- `docs/rules/springboot/flyway-db-migration-rule.md`
- `docs/rules/springboot/testing-rule.md`

## 불변조건 (Codex와 공동 검토로 확정)

1. 결과 저장이 가능한 정상 DB 상태에서는, 계산을 시도한 공고×회사가 기존 row 유무와 관계없이 SUCCESS 또는 FAILED 결과로 남는다. 저장 자체가 실패하면 오류 로그로 관측 가능해야 한다 (완전한 durable 보장은 이 Task 범위 밖 — 아래 잔여 리스크 참고).
2. FAILED 전환, 점수 null 초기화, `error_message` 기록은 하나의 트랜잭션에서 원자적으로 commit된다.
3. 계산 트랜잭션과 실패 기록 트랜잭션은 분리되어, 계산 예외가 FAILED 기록의 commit을 막지 않는다. (`markFailed()`는 일반 `@Transactional`(REQUIRED)로 충분 — 호출 시점엔 이전 `calculateAndSave()`의 트랜잭션이 이미 완전히 끝난 뒤라 활성 트랜잭션이 없음. `MatchCalculationStatusCoordinator.finish()`가 `REQUIRES_NEW`를 쓰는 건 그 메서드가 AFTER_COMMIT 동기 스레드에서도 호출되는 별도 경로가 있기 때문이며, `markFailed()`엔 그런 경로가 없음)
4. FAILED 기록 저장 자체가 실패하면 원래 계산 예외 + 저장 예외 + 공고/회사 식별자를 오류 로그로 남기고, 배치 제어 흐름에는 전파하지 않아 나머지 회사 처리를 계속한다 (이중 try-catch).
5. 한 회사의 실패는 이미 commit된 다른 회사 결과를 되돌리거나, 아직 처리되지 않은 회사의 계산을 중단시키지 않는다.
6. 이벤트가 원래 트랜잭션 안에서 발행되고 commit된 경우에만 매칭 작업이 제출된다. rollback되면 제출되지 않으며, 제출된 매칭 작업의 실패는 이미 commit된 원래 트랜잭션에 영향을 주지 않는다 (기존 `BidMatchEventListenerIntegrationTest`가 검증).
7. 재계산 성공 시 새 점수 저장, `SUCCESS` 전환, `error_message` 초기화가 하나의 트랜잭션에서 원자적으로 commit된다.
8. 서로 다른 트리거(신규 공고 수집 ↔ 프로필 재계산) 간 동시 갱신 경쟁은 이 Task 범위 밖 — Issue #53으로 분리.

**잔여 리스크 (문서화하고 받아들임, 해결 안 함)**: FAILED 기록 저장 자체가 실패하면(예: DB 순단) 그 시도는 로그에만 남고 DB엔 반영되지 않는다 (이전 상태 그대로 남음). 재시도/전체 롤백 둘 다 검토했으나 채택하지 않음 — 재시도는 효과 대비 복잡도가 안 맞고, 전체 롤백은 무관한 회사들의 정상 결과까지 날리는 더 나쁜 트레이드오프. 실제 프로덕션이라면 재조정(reconciliation) 배치 + 패턴 기반 알림으로 다루는 게 맞지만, 이 프로젝트 범위에서는 로그 기반 관측으로 충분하다고 판단.

## Done Condition

- [x] `bid_match_results`에 `status`(`SUCCESS`/`FAILED`) 컬럼과 `error_message` 컬럼이 Flyway 마이그레이션으로 추가된다
- [x] `total_score`/`grade`가 nullable로 전환되고, `status`-점수 일관성 CHECK 제약이 추가된다
- [x] 실패 시 이전에 성공한 점수가 있어도 유지하지 않고 점수 관련 컬럼을 모두 null로 초기화하며, `status=FAILED`와 `error_message`를 기록한다 (재계산 트리거 자체가 프로필/공고 데이터 변경을 의미하므로 이전 점수는 stale하다고 판단 — 사용자 확정)
- [x] `BidMatchEventListener`, `CompanyProfileMatchEventListener` 양쪽의 개별 계산 실패 catch 블록이 이중 catch 구조로 새 실패 기록 로직을 호출한다 (불변조건 4)
- [x] 이후 재계산이 성공하면 FAILED였던 row가 점수/`SUCCESS`/`error_message=null`로 완전히 갱신된다 (기존 `update()` 경로로 자연 복구)
- [x] Notion `02. 도메인/DB 설계`의 `bid_match_results` 테이블 설명이 갱신된다
- [x] 위 "테스트가 증명해야 할 것" 1~6번이 전부 작성된다

## Out of Scope

- `BidMatchEventListener`의 `@Async` → `TaskExecutor` 직접 주입 전환 (스레드풀 포화로 제출 자체가 거부되는 문제) → Issue #51에서 별도 처리, 이 Task의 스키마가 그 케이스도 표현 가능한지는 확인하되 리스너 구조 변경은 하지 않음
- 프론트엔드에서 "계산 실패" 상태 노출 → 후속 Task (Issue #40의 나머지 Next Step)
- 실패 건 자동 재시도 → 채택하지 않음 (계산 자체의 재시도는 결정적 실패 가능성이 높아 Issue #40 제안 방향에 이미 명시. FAILED 기록 저장 자체의 재시도도 검토했으나 효과 대비 복잡도가 안 맞아 채택 안 함)
- FAILED 기록 저장 자체가 실패했을 때 전체 배치 롤백 → 검토했으나 기각 (무관한 회사들의 정상 결과까지 날아가는 더 나쁜 트레이드오프, 애초에 회사별로 트랜잭션이 분리돼 있어 사후 롤백 자체가 불가능)
- 서로 다른 트리거 간 동시 갱신 경쟁(최신 실행이 오래된 실행에 덮일 수 있음) → Issue #53으로 분리
- 재조정(reconciliation) 배치, 패턴 기반 알림 등 완전한 durable 보장 인프라 → 이 프로젝트 규모에 과함, 로그 기반 관측으로 대체

## 테스트가 증명해야 할 것

1. 계산 실패 시 점수 필드가 전부 null, `status=FAILED`, `error_message`가 채워진 채로 저장된다 (기존 row 있을 때) — `MatchCalculationServiceTest` 단위
2. 기존 row가 없어도 실패 시 FAILED row가 새로 생성된다 — `MatchCalculationServiceTest` 단위
3. FAILED였던 row가 재계산에 성공하면 새 점수 + `SUCCESS` + `error_message=null`로 완전히 갱신된다 — `MatchCalculationServiceTest` 단위
4. DB가 `SUCCESS`인데 점수가 null이거나 `FAILED`인데 점수가 남아있는 row 저장을 거부한다 (CHECK 제약 실동작) — `BidMatchResultEntityMappingTest` DB 통합
5. 한 회사의 계산 실패가 다른 회사의 SUCCESS 저장을 막지 않고, 나머지 회사도 계속 처리된다 — `BidMatchEventListenerIntegrationTest` / `CompanyProfileMatchEventListenerTest` 확장
6. `markFailed()` 저장 자체가 예외를 던져도 전체 루프를 끊지 않고, 로그에 원본 계산 예외 + 저장 예외가 둘 다 남으며, 다음 회사는 정상 처리된다 — 위와 동일 파일에 케이스 추가

## 작업 항목

- [x] Flyway 마이그레이션: `status`/`error_message` 컬럼 추가, `total_score`/`grade` nullable 전환, 상태-점수 일관성 CHECK 제약 추가
- [x] `BidMatchResultStatus` enum 추가 (`SUCCESS`, `FAILED`)
- [x] `BidMatchResult` 엔티티: `status`/`errorMessage` 필드 추가, `markFailed()`/`createFailed()` 추가, 기존 `create()`/`update()`가 `status=SUCCESS`/`errorMessage=null`을 명시하도록 수정
- [x] `MatchCalculationService.markFailed()` 추가 (`@Transactional` REQUIRED, 기존 row 있으면 갱신, 없으면 FAILED row 신규 생성)
- [x] `MatchCalculationStatusCoordinator.markFailed()` passthrough 추가
- [x] `BidMatchEventListener` catch 블록에 이중 catch 구조로 `markFailed()` 호출 추가
- [x] `CompanyProfileMatchEventListener` catch 블록에 이중 catch 구조로 `coordinator.markFailed()` 호출 추가
- [x] 테스트 1~6번 작성
- [x] Notion `02. 도메인/DB 설계` 갱신
