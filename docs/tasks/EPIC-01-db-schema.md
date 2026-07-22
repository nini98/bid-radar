# EPIC-01 DB 스키마 설계 및 Flyway 마이그레이션

## 목표

Epic-1에 필요한 핵심 테이블 스키마를 Flyway 마이그레이션 파일로 작성하고, 대응하는 JPA 엔티티를 구현한다.

---

## 파트

backend

---

## 참조 Rule

- `docs/rules/springboot/architecture-rule.md`
- `docs/rules/springboot/entity-rule.md`
- `docs/rules/springboot/flyway-db-migration-rule.md`

---

## Done Condition

- [x] Flyway 마이그레이션 실행 시 대상 테이블이 모두 정상 생성된다
- [x] 인덱스가 정상 생성된다
- [x] `ddl-auto: validate` 상태에서 앱이 정상 기동된다
- [x] JPA 엔티티와 DB 테이블 컬럼이 일치한다

---

## Out of Scope

- 회사 프로필 관련 테이블 (companies, tech_tags 등) → Epic-2
- AI 분석 관련 테이블 (document_analyses, bid_analyses 등) → Epic-3
- 운영 이력 테이블 (collection_jobs, analysis_jobs) → Epic-4
- Repository, Service, API 구현 → 각 도메인 Task

---

## 작업 항목

- [x] `V2__create_users.sql` 작성
  - users 테이블 (id, email, password_hash, name, role, is_active, last_login_at, created_at, updated_at)
  - email UNIQUE 제약
- [x] `V3__create_bid_notices.sql` 작성
  - bid_notices 테이블 (전체 컬럼)
  - 인덱스: external_notice_id(UNIQUE), bid_deadline, published_at, status, agency, region
- [x] `V4__create_bid_attachments.sql` 작성
  - bid_attachments 테이블
  - bid_notice_id FK
- [x] `V5__create_favorites.sql` 작성
  - favorites 테이블
  - UNIQUE: (user_id, bid_notice_id)
- [x] `User` JPA 엔티티 작성
- [x] `BidNotice` JPA 엔티티 작성
- [x] `BidAttachment` JPA 엔티티 작성
- [x] `Favorite` JPA 엔티티 작성
