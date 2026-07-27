# EPIC-02 입찰방식/계약방식 Rule 픽스처·문서 정합성 정리

## 목표
BidTypeRule의 bidType/contractType 매칭 테스트 픽스처와 Notion 04번 문서의 필드 매핑을 나라장터 API 실제 값 기준으로 정리하고, 문자열 완전일치 비교 방식의 개선 필요 여부를 검토한다.

## 파트
backend

## 관련 Issue
- Issue: #19

담당 Next Step:
- [ ] Notion 04번 문서의 필드 매핑을 원문 기준으로 재확인
- [ ] 백엔드 테스트의 bidType/contractType 픽스처를 명세 값으로 정리
- [ ] 문자열 완전일치 방식에 정규화 또는 코드화가 필요한지 검토

## 참조 Rule
- docs/rules/springboot/architecture-rule.md
- docs/rules/springboot/entity-rule.md
- docs/rules/springboot/repository-rule.md
- docs/rules/springboot/transaction-rule.md
- docs/rules/springboot/flyway-db-migration-rule.md
- docs/rules/springboot/testing-rule.md

## Done Condition
- [x] Notion 04번 문서 §9 "입찰 방식 선호(10점)" 행이 실제 코드(BidTypeRule: 입찰방식 `bidMethdNm` 5점 + 계약방식 `cntrctCnclsMthdNm` 5점)와 일치하도록 정정됨
- [x] `BidTypeRuleTest` 등 bidType/contractType을 사용하는 백엔드 테스트 픽스처가 나라장터 API 실제 명세 값으로 정리됨
- [x] 문자열 완전일치 비교 방식 개선 필요 여부에 대한 검토 결론이 남겨짐 (아래 `## 검토 결론` 참고)

## Out of Scope
- 프론트엔드 `BID_TYPES`/`CONTRACT_TYPES` 상수 정정 및 누락 선택지 추가 — 별도 프론트엔드 Task에서 처리 예정, 이번 PR에는 포함하지 않음
- 코드화(문자열 대신 enum/코드 테이블로 전환) 자체의 구현 — 검토 결과 필요하다고 판단되더라도 이번 PR에서 구현하지 않고, 별도 Task로 분리해 착수 여부를 사용자에게 확인받음

## 작업 항목
- [x] 나라장터 API Word 명세서(`docs/조달청_OpenAPI참고자료_나라장터_입찰공고정보서비스_1.2.docx`)에서 `bidMethdNm`(입찰방식)/`cntrctCnclsMthdNm`(계약방식) 실제 값 목록 재확인
- [x] Notion 04번 문서 §9 표 정정 (입찰 방식 선호 10점 → 입찰방식 5점 + 계약방식 5점으로 분리, 각 데이터 출처 명시)
- [x] `BidTypeRuleTest` 등 백엔드 테스트 픽스처를 나라장터 실제 값으로 정리
- [x] 문자열 완전일치 비교 방식(정규화 vs 코드화) 검토 및 결론 작성

## 검토 결론

**결론: 정규화도 코드화도 현재는 필요 없음. 완전일치 비교를 유지한다.**

- `preferredBidTypes`/`preferredContractTypes`(회사 프로필 선호값)는 프론트 `BID_TYPES`/`CONTRACT_TYPES` 드롭다운에서 선택되는 값이라 자유 입력이 아니다.
- `bid.getBidType()`/`bid.getContractType()`(나라장터 응답값)도 API 명세서에 정의된 고정된 값 목록 안에서만 온다.
- 즉 비교 양쪽 모두 자유 텍스트가 아니라 동일한 고정 문자열 집합에서 나오므로, 공백/표기 차이로 매칭이 깨질 free-text 리스크가 없다. 프론트 상수를 스펙과 정확히 일치시키기만 하면(별도 프론트 Task) 완전일치로 충분하다.
- 정규화(trim 등)는 지금 관찰된 구체적 실패 사례가 없어 추가할 근거가 없다. 코드화(enum/코드 테이블)는 값이 늘어나거나 자주 바뀌는 경우에 이점이 있는데, 나라장터 값 목록은 API 명세서에 고정돼 있어 그 이점이 지금은 없다.
- 예외적으로 API 명세서 예시 데이터에는 `cntrctCnclsMthdNm`이 빈 문자열이거나 `"공고서참조"`인 경우가 있었다(주로 `bidMethdNm=직찰`일 때). `BidTypeRule`은 `actual == null`일 때만 "정보 없음"으로 처리하고, 빈 문자열/`"공고서참조"`는 선호 목록에 없으면 그냥 0점(불일치) 처리된다. 이는 크래시나 버그가 아니라 "매칭 안 됨"으로 정상 동작하는 것이라 이번 Task에서 별도 처리하지 않는다. 실제 운영 데이터에서 이로 인한 이상 동작이 관찰되면 그때 별도 이슈로 다룬다.
