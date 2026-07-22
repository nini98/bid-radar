# EPIC-02 회사 프로필 / 매칭 결과 DB 스키마

## 목표

회사 프로필, 코드 테이블(기술 태그/사업 분야), 매칭 결과 테이블을 Flyway 마이그레이션으로 추가하고 대응하는 JPA 엔티티를 구현한다.

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

- [x] Flyway 마이그레이션 실행 시 `tech_tags`, `business_areas`, `companies`, `company_tech_tags`, `company_business_areas`, `company_certificates`, `company_project_experiences`, `company_bid_preferences`, `bid_match_results` 테이블이 모두 정상 생성된다
- [x] `company_tech_tags(company_id, tech_tag_id)`, `company_business_areas(company_id, business_area_id)`, `company_bid_preferences(company_id)`, `bid_match_results(bid_notice_id, company_id)` UNIQUE 제약이 정상 동작한다
- [x] `ddl-auto: validate` 상태에서 앱이 정상 기동된다
- [x] JPA 엔티티와 DB 테이블 컬럼이 일치한다

---

## Out of Scope

- `document_analyses`, `bid_analyses` 테이블 → Epic-3
- `collection_jobs`, `analysis_jobs` 테이블 → Epic-4
- Repository, Service, API 구현 → 각 도메인 Task
- 코드 테이블 초기 데이터(시딩) → `EPIC-02-codes-api`

---

## 작업 항목

- [x] `V7__create_code_tables.sql` 작성
  - `tech_tags` (id, name UNIQUE)
  - `business_areas` (id, name UNIQUE)
- [x] `V8__create_companies.sql` 작성
  - `companies` 테이블 (id, user_id FK, company_name, business_number, industry, founded_year, company_size, region, address, website, strengths, manager_name, manager_email, manager_phone, narajangter_linked, created_at, updated_at)
  - `user_id` UNIQUE (1:1 관계)
- [x] `V9__create_company_sub_tables.sql` 작성
  - `company_tech_tags` (company_id FK, tech_tag_id FK), UNIQUE(company_id, tech_tag_id)
  - `company_business_areas` (company_id FK, business_area_id FK), UNIQUE(company_id, business_area_id)
  - `company_certificates` (id, company_id FK, certificate_name)
  - `company_project_experiences` (id, company_id FK, project_type, description)
  - `company_bid_preferences` (id, company_id FK UNIQUE, preferred_regions VARCHAR[], budget_min, budget_max, deadline_min_days, preferred_bid_types VARCHAR[], preferred_contract_types VARCHAR[])
- [x] `V10__create_bid_match_results.sql` 작성
  - `bid_match_results` 테이블 (id, bid_notice_id FK, company_id FK, total_score, grade, score_tech, score_region, score_budget, score_business, matched_keywords JSONB, score_reason, calculated_at, created_at)
  - UNIQUE(bid_notice_id, company_id)
- [x] `TechTag`, `BusinessArea` JPA 엔티티 작성
- [x] `Company` JPA 엔티티 작성 (하위 컬렉션은 `CompanyTechTag`, `CompanyBusinessArea`, `CompanyCertificate`, `CompanyProjectExperience` 연관관계로 매핑)
- [x] `CompanyBidPreference` JPA 엔티티 작성 (`Company`와 1:1)
- [x] `BidMatchResult` JPA 엔티티 작성
