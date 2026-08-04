# EPIC-04 감사 타임스탬프 시간대 정책 통일

## 목표

`BaseEntity`의 감사 타임스탬프(`created_at`/`updated_at`)를 `Instant`로 전환해, 로컬/운영 환경의 JVM 기본 타임존과 무관하게 항상 동일한 절대 시각을 기록·응답하도록 한다.

---

## 파트

backend + frontend

---

## 관련 Issue

- Issue: #47

담당 Next Step (생성 시점 원문 그대로 인용, Issue 본문과 동일하게 들여쓰기 없이 기록):
- [x] 백엔드 감사 타임스탬프(`BaseEntity` 등) 시간대 처리 정책 확정 및 반영 (Instant 전환, JVM 타임존 통일)
- [x] 관련 DTO 직렬화 수정 확인, 프론트 파싱 로직 재점검
- [x] Notion 02/03 문서에 시간 표현 정책 반영

---

## 참조 Rule

- `docs/rules/springboot/entity-rule.md`
- `docs/rules/springboot/config-property-rule.md`
- `docs/rules/springboot/dto-response-rule.md`
- `docs/rules/springboot/testing-rule.md`
- `docs/rules/springboot/flyway-db-migration-rule.md` (Codex 리뷰 P1 대응으로 TIMESTAMPTZ 마이그레이션 추가하며 범위 확장)
- `docs/rules/frontend/testing-rule.md`

---

## Done Condition

- [x] `BaseEntity.createdAt`/`updatedAt`이 `Instant` 타입으로 저장되어, JVM 기본 타임존과 무관하게 항상 동일한 절대 시각을 기록한다
- [x] ~~`hibernate.jdbc.time_zone=UTC` 설정으로, 기존 `TIMESTAMP`(오프셋 없음) 컬럼과 `Instant` 간 변환이 세션/환경 타임존에 의존하지 않고 항상 UTC 기준으로 이뤄진다~~ → Codex 리뷰(P1, 2건)로 폐기. 이 설정이 앱 전체의 모든 `TIMESTAMP`/`LocalDateTime` 매핑에 전역 적용되어 G2B 업무 날짜(`bid_notices.published_at` 등)까지 오염시켰고, 그와 무관하게 `MatchCalculationStatusRepository`의 `CURRENT_TIMESTAMP`(Postgres 세션 TZ 기준 평가)와도 근본적으로 안 맞았다. 대신 `BaseEntity`가 관리하는 6개 테이블의 컬럼 자체를 `TIMESTAMPTZ`로 이관해(V14 마이그레이션) 세션/환경 타임존과 무관하게 항상 같은 절대 시각을 보존하도록 하고, `hibernate.jdbc.time_zone` 설정은 제거했다
- [x] `MatchCalculationStatusRepository.acquireLock`/`acquireRetryLock`의 5분 락 유효기간(`staleBefore`) 비교가 `Instant` 기준으로 이뤄진다 (호출부인 `MatchCalculationStatusService.retry()`, `CompanyProfileService`의 락 획득부 포함)
- [x] `MatchCalculationStatusResponse.updatedAt`, `CompanyProfileResponse.updatedAt`이 `Instant`로 응답되어, JSON 직렬화 시 오프셋(`Z`) 포함 ISO-8601 문자열로 내려간다
- [x] ~~`backend/Dockerfile`과 로컬 `./gradlew bootRun` 양쪽의 JVM 기본 타임존이 UTC로 통일된다~~ → Codex 리뷰(P1)로 폐기. `Instant`는 타임존 비의존적이라 이 조건 자체가 불필요했고, JVM 기본 타임존을 전역으로 바꾸면 `BidNoticeRepositoryImpl`(오늘 집계, `deadlineDays`)·`BidCollectScheduler`(수집 대상 날짜, cron 실행 시각)처럼 한국 업무 시간 기준으로 동작해야 하는 로직까지 UTC로 밀려나는 회귀가 있었다. 대신 해당 지점들에 기존 `ClockConfig`(`Asia/Seoul`) `Clock` 빈을 명시적으로 주입하고 `@Scheduled`에 `zone = "Asia/Seoul"`을 지정했다
- [x] 프론트 `isLockStale`(`matchStatus.ts`)이 오프셋 포함 문자열(`...Z`)에서도 정상 동작함을 테스트로 확인한다
- [x] Notion `02. 도메인/DB 설계`, `03. API 명세`에 "감사 타임스탬프는 UTC 절대 시각으로 기록·응답, 프론트는 브라우저 로컬 타임존으로 해석·표시" 정책이 반영된다
- [x] (Codex 리뷰 P2 대응, 이후 P1 재지적으로 처리 방식 변경) `BaseEntity` 관리 6개 테이블(`users`, `bid_notices`, `bid_attachments`, `companies`, `company_bid_preferences`, `match_calculation_status`)의 `created_at`/`updated_at`이 `TIMESTAMPTZ`로 이관되어, 컬럼 타입 자체가 세션/환경 타임존과 무관하게 절대 시각을 보존한다 (V14 마이그레이션)

---

## Out of Scope

- `BaseEntity` 외의 비즈니스 도메인 타임스탬프(`BidMatchResult.calculatedAt`, `BidNotice`의 G2B 원천 날짜 필드, JWT 발급/만료 계산 등) — 이슈 #47은 감사 타임스탬프(`created_at`/`updated_at`)에 한정된 문제이며, 나머지는 별도 성격의 도메인 날짜다
- ~~DB 컬럼 타입을 `TIMESTAMPTZ`로 바꾸는 Flyway 마이그레이션~~ → Codex 리뷰(P1, `hibernate.jdbc.time_zone=UTC`가 G2B 업무 날짜까지 오염시키는 문제 + `CURRENT_TIMESTAMP`의 세션 TZ 의존 문제)로 스코프 안으로 들어옴. V14 마이그레이션으로 실제 적용
- `MatchCalculationStatusService`/`CompanyProfileService`에 신규 `Clock` 빈 주입 도입 — 기존에도 `LocalDateTime.now()` 직접 호출 방식이었으므로 `Instant.now()` 직접 호출로 일관성 유지 (오버엔지니어링 지양)
- 기존 비-UTC 환경(로컬 등)에 이미 쌓인 값을 실제 원래 시간대로 정확히 보정하는 작업(예: 행별로 어느 타임존에서 기록됐는지 역추적) — 사용자 확인 결과 생략. 근거: (1) 이 서비스는 RDS 없이 EC2 위 Docker Compose로 Postgres를 직접 운영하는데, 2026-07-19부로 EC2가 중지되어 현재 운영 트래픽이 없다. (2) `backend/Dockerfile`이 이번 PR 전까지 타임존을 명시한 적이 없고 `eclipse-temurin:21-jre-alpine`은 tzdata 부재로 JVM이 UTC로 폴백하는 경우가 많아, 운영 환경은 이번 변경 전에도 사실상 UTC 기준으로 기록됐을 가능성이 높다고 가정한다 (로컬 개발 환경만 호스트 타임존의 영향을 받았을 것). 이 가정에 따라 V14 마이그레이션은 기존 값을 UTC 벽시각으로 해석해 타입만 전환한다 (값 자체는 보정하지 않음). 이 가정은 검증되지 않았으므로, 추후 EC2를 재기동해 실제 운영 데이터를 확인할 기회가 있으면 재검토한다

---

## 작업 항목

- [x] `BaseEntity.createdAt`/`updatedAt`을 `Instant`로 변경
- [x] ~~`application.yml`에 `spring.jpa.properties.hibernate.jdbc.time_zone: UTC` 추가~~ → Codex 리뷰(P1)로 제거 (위 Done Condition 참고)
- [x] (Codex 리뷰 P1 대응) `V14__migrate_audit_timestamps_to_timestamptz.sql` 추가 — `BaseEntity` 관리 6개 테이블의 `created_at`/`updated_at`을 `TIMESTAMP` → `TIMESTAMPTZ`로 전환 (`AT TIME ZONE 'UTC'`로 기존 값 해석)
- [x] `CompanyProfileMatchEventListenerIntegrationTest`의 raw JDBC 갱신을 `LocalDateTime` → `OffsetDateTime`(UTC)으로 변경 — `TIMESTAMPTZ` 컬럼에 오프셋 없는 값을 바인딩하면 세션 TZ에 의존하는 동일한 문제가 테스트 안에 재발할 수 있어서
- [x] `MatchCalculationStatusRepository.acquireLock`/`acquireRetryLock`의 `staleBefore` 파라미터 타입을 `Instant`로 변경
- [x] `MatchCalculationStatusService.retry()`, `CompanyProfileService`(락 획득부)의 `staleBefore` 계산을 `Instant.now().minus(Duration.ofMinutes(...))`로 변경
- [x] `MatchCalculationStatusResponse.updatedAt`, `CompanyProfileResponse.updatedAt`을 `Instant`로 변경
- [x] ~~`BidRadarBackendApplication`(main)과 `backend/Dockerfile`에 JVM 기본 타임존 UTC 설정 추가~~ → Codex 리뷰(P1)로 되돌림 (위 Done Condition 참고)
- [x] (Codex 리뷰 P1 대응) `BidNoticeRepositoryImpl`(오늘 집계, `deadlineDays`), `BidCollectScheduler`(수집 대상 날짜, cron `zone`)에 기존 `ClockConfig`의 `Asia/Seoul` `Clock` 빈을 명시적으로 주입
- [x] 백엔드 관련 테스트 갱신 (타입 변경에 따른 컴파일/픽스처 수정, CAS 락 5분 경계값 테스트 유지, 응답 직렬화가 오프셋 포함 문자열인지 확인하는 테스트 추가, `Clock` 빈 주입에 따라 `@DataJpaTest` 슬라이스 테스트 6개에 `ClockConfig` import 추가)
- [x] 프론트 `matchStatus.test.ts`에 오프셋 포함 문자열(`...Z`) 케이스 추가
- [x] Notion `02. 도메인/DB 설계`, `03. API 명세` 갱신
