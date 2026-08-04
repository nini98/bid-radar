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
- `docs/rules/frontend/testing-rule.md`

---

## Done Condition

- [x] `BaseEntity.createdAt`/`updatedAt`이 `Instant` 타입으로 저장되어, JVM 기본 타임존과 무관하게 항상 동일한 절대 시각을 기록한다
- [x] `hibernate.jdbc.time_zone=UTC` 설정으로, 기존 `TIMESTAMP`(오프셋 없음) 컬럼과 `Instant` 간 변환이 세션/환경 타임존에 의존하지 않고 항상 UTC 기준으로 이뤄진다
- [x] `MatchCalculationStatusRepository.acquireLock`/`acquireRetryLock`의 5분 락 유효기간(`staleBefore`) 비교가 `Instant` 기준으로 이뤄진다 (호출부인 `MatchCalculationStatusService.retry()`, `CompanyProfileService`의 락 획득부 포함)
- [x] `MatchCalculationStatusResponse.updatedAt`, `CompanyProfileResponse.updatedAt`이 `Instant`로 응답되어, JSON 직렬화 시 오프셋(`Z`) 포함 ISO-8601 문자열로 내려간다
- [x] `backend/Dockerfile`과 로컬 `./gradlew bootRun` 양쪽의 JVM 기본 타임존이 UTC로 통일된다
- [x] 프론트 `isLockStale`(`matchStatus.ts`)이 오프셋 포함 문자열(`...Z`)에서도 정상 동작함을 테스트로 확인한다
- [x] Notion `02. 도메인/DB 설계`, `03. API 명세`에 "감사 타임스탬프는 UTC 절대 시각으로 기록·응답, 프론트는 브라우저 로컬 타임존으로 해석·표시" 정책이 반영된다

---

## Out of Scope

- `BaseEntity` 외의 비즈니스 도메인 타임스탬프(`BidMatchResult.calculatedAt`, `BidNotice`의 G2B 원천 날짜 필드, JWT 발급/만료 계산 등) — 이슈 #47은 감사 타임스탬프(`created_at`/`updated_at`)에 한정된 문제이며, 나머지는 별도 성격의 도메인 날짜다
- DB 컬럼 타입을 `TIMESTAMPTZ`로 바꾸는 Flyway 마이그레이션 — `hibernate.jdbc.time_zone=UTC` 설정만으로 기존 `TIMESTAMP` 컬럼과의 일관된 변환이 가능해 스키마 변경 없이 해결한다
- `MatchCalculationStatusService`/`CompanyProfileService`에 신규 `Clock` 빈 주입 도입 — 기존에도 `LocalDateTime.now()` 직접 호출 방식이었으므로 `Instant.now()` 직접 호출로 일관성 유지 (오버엔지니어링 지양)

---

## 작업 항목

- [x] `BaseEntity.createdAt`/`updatedAt`을 `Instant`로 변경
- [x] `application.yml`에 `spring.jpa.properties.hibernate.jdbc.time_zone: UTC` 추가
- [x] `MatchCalculationStatusRepository.acquireLock`/`acquireRetryLock`의 `staleBefore` 파라미터 타입을 `Instant`로 변경
- [x] `MatchCalculationStatusService.retry()`, `CompanyProfileService`(락 획득부)의 `staleBefore` 계산을 `Instant.now().minus(Duration.ofMinutes(...))`로 변경
- [x] `MatchCalculationStatusResponse.updatedAt`, `CompanyProfileResponse.updatedAt`을 `Instant`로 변경
- [x] `BidRadarBackendApplication`(main) 또는 `build.gradle`의 `bootRun`/`test` 태스크와 `backend/Dockerfile`에 JVM 기본 타임존 UTC 설정 추가
- [x] 백엔드 관련 테스트 갱신 (타입 변경에 따른 컴파일/픽스처 수정, CAS 락 5분 경계값 테스트 유지, 응답 직렬화가 오프셋 포함 문자열인지 확인하는 테스트 추가)
- [x] 프론트 `matchStatus.test.ts`에 오프셋 포함 문자열(`...Z`) 케이스 추가
- [x] Notion `02. 도메인/DB 설계`, `03. API 명세` 갱신
