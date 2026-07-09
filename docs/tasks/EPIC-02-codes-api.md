# EPIC-02 코드 조회 API (기술 태그 / 사업 분야)

## 목표

회사 프로필 설정에서 사용할 기술 태그, 사업 분야 코드 목록 조회 API를 구현하고 초기 코드 데이터를 시딩한다.

---

## 파트

Spring Boot

---

## 참조 Rule

- `docs/rules/springboot/architecture-rule.md`
- `docs/rules/springboot/api-response-rule.md`
- `docs/rules/springboot/dto-response-rule.md`
- `docs/rules/springboot/flyway-db-migration-rule.md`

---

## Done Condition

- [x] `GET /api/codes/tech-tags` 요청 시 기술 태그 목록이 반환된다
- [x] `GET /api/codes/business-areas` 요청 시 사업 분야 목록이 반환된다
- [x] 두 응답 모두 공통 Wrapper를 사용한다
- [x] 초기 코드 데이터가 마이그레이션으로 시딩되어 있다

---

## Out of Scope

- 코드 관리자 화면(CRUD) → 향후 관리자 기능
- 회사 프로필과의 연결(저장) → `EPIC-02-company-profile-api`

---

## 작업 항목

- [x] `V11__seed_code_tables.sql` 작성
  - `tech_tags` 초기 데이터 (Java, Spring, Spring Boot, Python, React, Vue.js, Node.js, AWS, Azure, Docker, Kubernetes, MSA, QueryDSL, PostgreSQL, MySQL, Oracle, AI, 빅데이터 — 18개)
  - `business_areas` 초기 데이터 (SI, SM(유지보수), 공공SI, 스마트팩토리, 관제시스템, 클라우드, AI, 빅데이터, IoT, 정보보안 — 10개)
- [x] `TechTagRepository`, `BusinessAreaRepository` 구현
- [x] `CodeResponse` DTO 작성 (id, name)
- [x] `CodeService` 구현 (전체 목록 조회)
- [x] `CodeController` 구현
  - `GET /api/codes/tech-tags`
  - `GET /api/codes/business-areas`
