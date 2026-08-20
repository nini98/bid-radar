# EPIC-02 공고 분류 필드를 qualification_summary로 매핑

## 목표

G2B 목록 API(건설/용역/물품) 응답에 이미 포함되어 있으나 수집되지 않던 분류 필드(주공종명/공공조달분류명/세부품명)를 `qualification_summary`에 채워, `TechTagMatchRule`/`BusinessAreaMatchRule`이 title 외 추가 텍스트로도 매칭할 수 있게 한다.

## 파트

backend

## 관련 Issue

- Issue: #39

담당 Next Step (생성 시점 원문 그대로 인용):
- [ ] TechTagMatchRule/BusinessAreaMatchRule에 사용할 공고 데이터 출처를 확정하고 수집·매핑 로직 추가

## 참조 Rule

- `docs/rules/springboot/mapper-rule.md`
- `docs/rules/springboot/entity-rule.md`
- `docs/rules/springboot/testing-rule.md`

## 불변조건

1. 이 태스크는 항상 null이던 `qualification_summary`를 채우는 것뿐이며, 기존 스키마·API 계약·다른 룰(`RegionRestrictionRule`/`IndustryRestrictionRule` 등)의 동작은 건드리지 않는다.
2. 건설/용역/물품 세 공고유형 중 그 공고가 실제로 어느 목록 API에서 왔는지에 해당하는 값만 채택한다 — 다른 유형의 필드를 임의로 섞어 쓰지 않는다 (원본 값 그대로, 가공·요약 없음).
3. 이미 수집되어 저장된 기존 공고(현재 `qualification_summary=null`인 row)는 이 변경으로 소급 갱신되지 않는다 — 신규 수집분부터만 적용된다 (개발 단계, backfill 불필요로 사용자 확정). `qualification_summary`는 지금까지 모든 row가 예외 없이 null이었고 프론트(`BidDetailInfoSection.tsx`)와 룰 코드 둘 다 이미 null-safe하게 처리하므로, 신규/기존 row가 혼재해도 화면·기능 오작동은 없다.
4. `TechTagMatchRule`/`BusinessAreaMatchRule`의 매칭 로직 자체는 변경하지 않는다 — 데이터가 채워지는 것만으로 재검증이 성립한다.

## 결정 목록

1. **결정**: `G2bNoticeItem`에 `mainCnsttyNm`(건설)/`pubPrcrmntClsfcNm`(용역)/`dtilPrdctClsfcNoNm`(물품) 3개 필드를 그대로 추가하고, `G2bNoticeMapper.toCommand()`에서 coalesce(첫 번째 non-null 값 채택)해 하나의 `qualificationSummary`로 합친다.
   - **이유**: 각 목록 API 응답엔 자기 유형 필드만 존재하고 나머지는 응답 자체에 없어 자동으로 null이 됨 → 어떤 공고유형인지 별도 추적 없이 coalesce만으로 안전하게 값 하나를 얻을 수 있음. `BidCollectorService`가 이미 3개 API 결과를 한 리스트로 합쳐 처리하는 구조(개별 아이템에 "어느 오퍼레이션에서 왔는지" 정보가 안 남음)와도 맞음.
   - **틀렸을 때 나타나는 현상**: 만약 실제 응답에서 2개 이상 필드가 동시에 값을 가지면, 정한 우선순위 필드만 조용히 채택되고 나머지는 버려짐(크래시는 없음, 방어적으로 로그 경고만 남김).
   - **대안**: `BidCollectorService`가 fetch 결과에 유형 태그를 붙여 넘기는 구조로 바꿔 명시적으로 매핑 → 3개 fetch 호출부/데이터 흐름을 다 바꿔야 해서 이번 태스크 범위를 벗어남. 채택 안 함.
2. **결정**: 새 컬럼을 만들지 않고 기존 `qualification_summary` 컬럼을 그대로 재사용한다.
   - **이유**: `docs/tasks/EPIC-02-match-rule-engine.md`에 이미 "원래 데이터 출처인 조달분류(`pubPrcrmntClsfcNm`)"라고 명시돼 있어, 이 컬럼이 애초에 이 용도로 설계됐던 게 확인됨. 마이그레이션 불필요. Epic-3에서 이 컬럼을 다른 용도(AI 추출 텍스트 등)로 쓸 계획도 없음을 사용자에게 확인함.
   - **틀렸을 때 나타나는 현상**: 해당 없음 (계획 부재 확인됨).
   - **대안**: `classification_name` 신규 컬럼 추가. 채택 안 함.

## Done Condition

- [x] `G2bNoticeItem`에 분류 필드 3종이 추가된다
- [x] `G2bNoticeMapper.toCommand()`가 공고유형에 맞는 분류 필드 값을 coalesce해 `qualificationSummary`로 매핑한다
- [x] `BidNoticeCreateCommand`에 `qualificationSummary` 필드가 추가된다 (끝에 append)
- [x] `BidNotice.create(cmd)`가 `qualificationSummary`를 설정한다
- [x] `G2bNoticeMapperTest`가 신규 작성되어 매핑/coalesce 동작을 증명한다
- [x] 아래 "테스트가 증명해야 할 것" 항목이 전부 작성된다
- [x] `BidNoticeCreateCommand` 생성자를 직접 호출하는 기존 테스트 파일이 신규 필드 추가로 인한 컴파일 오류 없이 모두 통과한다 (계획에 없던 `BidNoticeProcessorTest.java`도 `G2bNoticeItem` 생성자를 직접 호출 중인 게 뒤늦게 발견되어 함께 수정)

## Out of Scope

- 이미 수집된 기존 `bid_notices`의 `qualification_summary` backfill → 신규 수집분부터만 적용 (불변조건 3)
- `TechTagMatchRule`/`BusinessAreaMatchRule` 룰 로직 자체 변경 → 이미 `qualificationSummary`를 검색 대상으로 삼고 있어 변경 불필요
- `RegionRestrictionRule`/`IndustryRestrictionRule` 관련 작업(면허제한/참가가능지역 상세 연동) → Issue #39의 별도 Task(B/C)로 분리

## 테스트가 증명해야 할 것

1. 건설/용역/물품 각 유형의 분류 필드(`mainCnsttyNm`/`pubPrcrmntClsfcNm`/`dtilPrdctClsfcNoNm`)가 `qualificationSummary`로 올바르게 매핑된다 — `G2bNoticeMapperTest` 단위
2. 세 필드가 모두 null이면 `qualificationSummary`도 null로 유지된다 — `G2bNoticeMapperTest` 단위

테스트 생략: `TechTagMatchRule`/`BusinessAreaMatchRule` 자체 — 룰 로직 변경 없음, 기존 테스트가 이미 `qualificationSummary` 유/무 케이스를 커버 중.

## 작업 항목

- [x] `G2bNoticeItem`에 `mainCnsttyNm`/`pubPrcrmntClsfcNm`/`dtilPrdctClsfcNoNm` 필드 추가
- [x] `G2bNoticeMapper.toCommand()`에 coalesce 로직 추가 (동시 값 존재 시 로그 경고)
- [x] `BidNoticeCreateCommand`에 `qualificationSummary` 필드 추가
- [x] `BidNotice.create(cmd)`에서 `qualificationSummary` 설정
- [x] `G2bNoticeMapperTest` 신규 작성 (테스트 1~2번)
- [x] `BidNoticeCreateCommand` 생성자를 직접 호출하는 기존 테스트 18개 파일 컴파일 유지 (인자 추가). 계획에 없던 `BidNoticeProcessorTest.java`(`G2bNoticeItem` 생성자 직접 호출)도 같은 이유로 함께 수정 — 범위 확장: 신규 필드 추가로 인한 기계적 컴파일 유지, 로직 변경 없음
- [x] `TechTagMatchRuleTest`/`BusinessAreaMatchRuleTest`의 `ReflectionTestUtils.setField` 우회 주입을 `BidNoticeCreateCommand` 직접 주입으로 정리 (동작 변경 없는 리팩터)
