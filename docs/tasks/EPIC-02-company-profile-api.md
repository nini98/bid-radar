# EPIC-02 회사 프로필 API

## 목표

회사 프로필 조회/저장 API(`GET`, `PUT /api/companies/me`)를 구현한다. 기술 태그, 사업 분야, 보유 인증, 프로젝트 경험, 입찰 선호 조건을 함께 관리한다.

---

## 파트

Spring Boot

---

## 참조 Rule

- `docs/rules/springboot/architecture-rule.md`
- `docs/rules/springboot/repository-rule.md`
- `docs/rules/springboot/api-response-rule.md`
- `docs/rules/springboot/dto-request-rule.md`
- `docs/rules/springboot/dto-response-rule.md`
- `docs/rules/springboot/mapper-rule.md`
- `docs/rules/springboot/exception-rule.md`
- `docs/rules/springboot/transaction-rule.md`

---

## Done Condition

- [x] `GET /api/companies/me` 요청 시 로그인한 사용자의 회사 프로필이 반환된다
- [x] 프로필이 없는 사용자가 조회 시 `data: null` 형태로(에러 아님) 응답된다
- [x] `PUT /api/companies/me` 요청 시 회사 프로필이 생성 또는 수정된다
- [x] `techTagIds`, `businessAreaIds`로 전달한 코드 ID가 하위 테이블에 정상 반영된다
- [x] 존재하지 않는 `techTagIds`/`businessAreaIds` 전달 시 400 응답이 반환된다
- [x] 모든 응답이 공통 Wrapper를 사용한다

---

## Out of Scope

- 적합도 재계산 트리거 → `EPIC-02-match-recalculate`
- 공고 목록/상세에서 매칭 결과 노출 → `EPIC-02-bid-list-match-integration`
- 회사 프로필 설정 화면(Frontend) → `EPIC-02-company-profile-frontend`

---

## 작업 항목

- [x] `CompanyRepository` 구현 (`findByUserId`)
- [x] `CompanyTechTagRepository`, `CompanyBusinessAreaRepository`, `CompanyCertificateRepository`, `CompanyProjectExperienceRepository`, `CompanyBidPreferenceRepository` 구현
- [x] `CompanyProfileRequest` DTO 작성 (companyName, businessNumber, industry, foundedYear, companySize, region, address, website, strengths, techTagIds, businessAreaIds, certificates, projectExperiences, bidPreference, managerName, managerEmail, managerPhone — Bean Validation 포함)
- [x] `CompanyProfileResponse` DTO 작성 (03 API 명세의 응답 구조 그대로: techTags, businessAreas, certificates, projectExperiences, bidPreference 포함)
- [x] `CompanyProfileService` 구현
  - 조회: `user_id` 기준 회사 없으면 `null` 반환
  - 저장: 회사 없으면 신규 생성, 있으면 수정 (하위 컬렉션은 전체 교체 방식)
  - `techTagIds`/`businessAreaIds` 유효성 검증 (존재하지 않는 ID → `ApiException` BAD_REQUEST)
- [x] `CompanyProfileController` 구현
  - `GET /api/companies/me`
  - `PUT /api/companies/me`
