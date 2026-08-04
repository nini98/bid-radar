-- BaseEntity(감사 컬럼: created_at/updated_at)를 상속하는 6개 테이블의 컬럼 타입을
-- TIMESTAMP(오프셋 없음) -> TIMESTAMPTZ로 전환한다.
--
-- 배경: created_at/updated_at이 엔티티에서 Instant로 매핑되는데, TIMESTAMP 컬럼은
-- Postgres 세션의 TimeZone 설정에 따라 CURRENT_TIMESTAMP 평가/값 표시가 달라져
-- 세션 TZ가 UTC가 아니면 Instant로 읽어들일 때 절대 시각이 어긋난다. TIMESTAMPTZ는
-- 세션 TZ와 무관하게 항상 동일한 절대 시각을 보존하므로 이 문제가 근본적으로 사라진다.
--
-- 기존 값 처리: 이 서비스는 RDS 없이 EC2 위 Docker Compose로 Postgres를 직접
-- 운영하며 2026-07-19부로 EC2가 중지되어 운영 트래픽이 없고, backend/Dockerfile이
-- 지금까지 타임존을 명시한 적이 없어(eclipse-temurin:21-jre-alpine, tzdata 부재)
-- 운영 환경은 이미 UTC 기준으로 기록됐을 가능성이 높다고 가정한다(검증되지 않은
-- 가정 - Issue #47, docs/tasks/EPIC-04-audit-timestamp-timezone.md 참고). 따라서
-- 기존 TIMESTAMP 값을 UTC 벽시각으로 해석해 타입만 전환한다.

ALTER TABLE users
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE bid_notices
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE bid_attachments
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE companies
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE company_bid_preferences
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE match_calculation_status
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC';
