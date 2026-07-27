# EPIC-02 프론트 입찰방식 라벨 정정 및 선택지 정리

## 목표
프론트 `BID_TYPES`의 오타 라벨(`전자시담(2인 이상)`)을 나라장터 명세 값(`전자시담(다자간)`)으로 정정하고, 누락된 입찰방식 선택지 추가 여부를 결정한다.

## 파트
frontend + backend

## 관련 Issue
- Issue: #19

담당 Next Step:
- [ ] 프론트 `BID_TYPES`의 `전자시담(2인 이상)`을 `전자시담(다자간)`으로 정정
- [ ] 누락된 입찰방식의 프론트 선택지 추가 여부 결정

## 참조 Rule
- docs/rules/frontend/frontend-rule.md
- docs/rules/frontend/testing-rule.md
- docs/rules/springboot/dto-request-rule.md (§4-2 범위 확장 대응)

## Done Condition
- [x] `frontend/src/constants/bidPreference.ts`의 `BID_TYPES`에서 `전자시담(2인 이상)` → `전자시담(다자간)`으로 정정됨
- [x] 누락된 입찰방식 선택지(`전자입찰/직찰`, `전자/직찰/우편/상시`, `복수견적(역경매)`) 추가 여부가 결정되고 반영됨 (결정: 3개 전부 추가, 명세에 있는 값을 임의로 제외할 근거 없음)
- [x] (범위 확장) `backend CompanyProfileRequest`의 `@AllowedValues`에서 임시 호환값 `전자시담(2인 이상)` 제거됨 (이전 Task의 "후속 조치" 예고 항목)
- [x] 위 백엔드 변경에 맞춰 `CompanyProfileControllerTest`의 호환값 관련 테스트가 갱신됨

## Out of Scope
- 문자열 완전일치 비교 방식 자체의 변경 (Issue #19에서 이미 검토 완료, 유지로 결론)
- Notion 04번 문서 추가 수정 (이미 완료됨)
- `REGIONS`/`BID_TYPES`/`CONTRACT_TYPES`를 백엔드 codes API로 옮기는 것 (별도 설계 논의 필요, 이번 Task 범위 아님)

## 작업 항목
- [x] `frontend/src/constants/bidPreference.ts`의 `BID_TYPES` 정정 + 누락 선택지 추가
- [x] `backend/.../CompanyProfileRequest.java`의 `@AllowedValues` 목록에서 임시 호환값 제거
- [x] `backend/.../CompanyProfileControllerTest.java`의 호환값 테스트 갱신 (구 값은 거부, 신 값은 허용 확인)
- [x] (범위 확장) `docs/rules/frontend/frontend-rule.md`에 "정적 선택지 데이터의 출처 판단" 기준(3-1) 추가 — 별도 PR 없이 이번 브랜치에 포함 (사용자 승인)
