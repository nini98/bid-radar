# EPIC-02 회사 프로필 설정 화면 (Frontend)

## 목표

회사 프로필을 조회/수정할 수 있는 화면을 React로 구현한다. 기술 태그/사업 분야 다중 선택, 보유 인증/프로젝트 경험 관리, 입찰 선호 조건 입력을 포함한다.

---

## 파트

frontend

---

## 참조 Rule

- `docs/rules/frontend/frontend-rule.md`

---

## Done Condition

- [ ] 기존 프로필이 있으면 값이 채워진 상태로 화면이 표시된다
- [ ] 프로필이 없는 신규 사용자는 빈 폼으로 표시된다
- [ ] 기술 태그 / 사업 분야를 코드 API 기반으로 다중 선택할 수 있다
- [ ] 보유 인증, 프로젝트 경험을 추가/삭제할 수 있다
- [ ] 입찰 선호 조건(선호 지역, 예산 범위, 마감 여유일, 선호 입찰/계약 방식)을 입력할 수 있다
- [ ] 저장 시 `PUT /api/companies/me` 호출 후 성공/실패 피드백이 표시된다
- [ ] 저장 성공 시 `POST /api/companies/me/recalculate`가 함께 호출되어 재계산이 트리거된다

---

## Out of Scope

- 공고 목록/상세 화면에 매칭 결과 노출 → `EPIC-02-bid-list-detail-frontend`
- 로그인 / 회원가입 화면 (Epic-1에서 이미 구현)
- 재계산 진행 상태 폴링 UI (상태 추적 필드가 없으므로 "재계산을 요청했습니다" 수준의 안내만 제공)

---

## 작업 항목

- [ ] `useCompanyProfile` 훅 구현 (`GET /api/companies/me`)
- [ ] `useCodes` 훅 구현 (기술 태그, 사업 분야 코드 조회)
- [ ] `useSaveCompanyProfile` 훅 구현 (`PUT /api/companies/me` 성공 시 `POST /api/companies/me/recalculate` 호출)
- [ ] `CompanyProfilePage` 컴포넌트 구현
- [ ] `TechTagSelect`, `BusinessAreaSelect` 다중 선택 컴포넌트 구현
- [ ] `CertificateList`, `ProjectExperienceList` 추가/삭제 가능한 리스트 컴포넌트 구현
- [ ] `BidPreferenceForm` 컴포넌트 구현 (선호 지역, 예산 범위, 마감 여유일, 선호 입찰/계약 방식)
- [ ] 저장 성공/실패 피드백 UI (토스트 등)
- [ ] 라우팅 등록 (`/company/profile`)
