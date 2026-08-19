# EPIC-04 BidMatchEventListener 스레드풀 제출 거부 시 실패 기록

## 목표

`BidMatchEventListener`가 `@Async` 대신 `TaskExecutor`를 직접 주입해 제출하고 `TaskRejectedException`을 캐치하도록 전환해, 스레드풀 포화로 제출 자체가 거부돼도 해당 공고 × 전체 회사가 FAILED로 기록되게 한다.

## 파트

backend

## 관련 Issue

- Issue: #51

담당 Next Step (생성 시점 원문 그대로 인용):
- [ ] #40의 `BidMatchResult` 실패 상태 스키마 결정이 이 케이스(스레드풀 제출 거부로 회사 목록조차 순회 못 한 경우)까지 포함하는지 확인
- [ ] `BidMatchEventListener`를 `TaskExecutor` 직접 주입 + `TaskRejectedException` 캐치 방식으로 전환
- [ ] 제출 거부 시 실패 기록 방식 구현 및 반영

## 참조 Rule

- `docs/rules/springboot/architecture-rule.md`
- `docs/rules/springboot/transaction-rule.md`
- `docs/rules/springboot/testing-rule.md`

## Done Condition

- [x] (Next Step 1 확인 결과 문서화) `MatchCalculationService.markFailed(bid, company, errorMessage)`가 이미 (공고, 회사) 단위 FAILED 기록을 지원하며, 스키마 변경 없이 이 케이스에 재사용 가능함을 확인 — Issue #40에서 추가된 메서드를 그대로 재사용, 스키마 변경 불필요
- [x] `BidMatchEventListener`가 `@Async` 애노테이션 대신 `TaskExecutor`(`matchTaskExecutor`)를 직접 주입해 제출한다
- [x] 제출이 `TaskRejectedException`으로 거부되면, 해당 공고를 다시 조회해 전체 회사 각각에 대해 FAILED 상태(`markFailed`)를 기록한다
- [x] 공고 조회 자체가 실패해도(공고가 이미 삭제됨 등) 예외가 전파되지 않고 로그로 남는다 (기존 `handle()`의 null 체크와 동일한 성격)

## Out of Scope

- `BidMatchResult`/실패 상태 표현 스키마 변경 — Issue #40에서 이미 완료, 이번 Task는 재사용만 한다
- 서로 다른 트리거 간 동시 갱신 경쟁(Issue #53) — 별도 Task
- `matchTaskExecutor` 풀 크기/큐 용량 튜닝 — 범위 밖

## 작업 항목

- [x] `BidMatchEventListener`에 `TaskExecutor matchTaskExecutor` 필드 주입, `@Async` 제거
- [x] `handle()`을 제출 전담으로 재구성: `matchTaskExecutor.execute(() -> process(event))`를 try, `TaskRejectedException` catch
- [x] 기존 `handle()` 본문(공고 조회 + 회사별 계산/실패 기록 루프)을 `process(event)`로 이동
- [x] `TaskRejectedException` catch 블록에서 공고 재조회 + 전체 회사에 `matchCalculationService.markFailed()` 호출
- [x] 테스트 작성 (아래 목록)
- [x] self-review + `springboot-review-rule.md` 점검
