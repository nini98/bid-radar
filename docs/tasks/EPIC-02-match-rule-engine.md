# EPIC-02 적합도 Rule Engine

## 목표

회사 프로필과 공고 정보를 비교해 룰 기반으로 적합도 점수(0~100점)와 등급을 계산하는 Rule Engine을 구현한다. AI 분석 없이 `bid_notices` 필드와 회사 프로필만으로 동작해야 한다.

---

## 파트

backend

---

## 참조 Rule

- `docs/rules/springboot/architecture-rule.md`
- `docs/rules/springboot/repository-rule.md`
- `docs/rules/springboot/transaction-rule.md`
- `docs/rules/springboot/testing-rule.md`

---

## Done Condition

- [x] 회사 프로필 + 공고 정보를 입력하면 0~100점 사이 총점이 계산된다
- [x] 동일한 입력에 대해 항상 동일한 점수가 계산된다 (결정론성)
- [x] 총점 80점 이상 `STRONG_REVIEW`, 60~79점 `RECOMMENDED`, 60점 미만 `NEED_REVIEW` 등급이 부여된다
- [x] `score_tech`, `score_region`, `score_budget`, `score_business` 세부 점수가 `bid_match_results`에 저장된다
- [x] `score_reason`에 점수 계산 근거가 텍스트로 기록된다
- [x] 새 `ScoreRule` 구현체를 추가하는 것만으로 점수 정책을 확장할 수 있는 구조다

---

## Out of Scope

- AI 분석 결과에 의존하는 룰(자격요건 충족, 공동수급 가능 여부, 리스크 감점) → Epic-3
- 매칭 계산을 언제 실행할지(수집 후 자동 계산, 재계산 API) → `EPIC-02-match-recalculate`
- 매칭 결과를 공고 목록/상세 API 응답에 노출 → `EPIC-02-bid-list-match-integration`

---

## 작업 항목

- [x] `ScoreRule` 인터페이스 정의 — `ScoreResult calculate(BidNotice bid, Company company, CompanyProfileContext profile)`. `Company`에는 `company_bid_preferences` 연관관계가 없어 (`@OneToOne(mappedBy)` 추가는 이 프로젝트에 bytecode enhancement가 없어 실제로는 즉시 로딩되는 부작용이 있어 배제) `CompanyProfileContext`로 회사 하위 데이터(기술태그/사업분야/예산·지역·마감·입찰유형 선호)를 조립해 전달하는 방식으로 확정
- [x] `ScoreResult` 값 객체 작성 (ruleName, score, maxScore, reason, matched)
- [x] `TechTagMatchRule` 구현 (+20) — 보유 기술 태그와 공고명/자격요건 요약 텍스트 일치 여부
- [x] `BusinessAreaMatchRule` 구현 (+20) — 보유 사업 분야와 공고명/자격요건 요약 텍스트 일치 여부. 원래 데이터 출처인 조달분류(`pubPrcrmntClsfcNm`)는 G2B 수집기가 수집하지 않아(별도 후속 태스크 필요) 텍스트 근사 매칭으로 대체
- [x] `BudgetRangeRule` 구현 (+15) — `company_bid_preferences.budget_min/budget_max` 범위 기준 (범위 내 만점, 50% 이내 초과 시 부분 점수, 크게 벗어나면 0점). 선호 미설정/공고 예산 없음은 만점 처리
- [x] `RegionMatchRule` 구현 (+10) — `preferred_regions` vs `bid_notices.region`. 선호 미설정/공고 지역 없음은 만점 처리
- [x] `IndustryRestrictionRule` 구현 (+10) — `industry_restriction`은 실제로는 업종 제한 카테고리가 아니라 Y/N 플래그(나라장터 API 명세 확인)라 "회사 자격과 부합" 비교가 불가능함. N/공백이면 만점, Y면 회사 자격 확인이 불가능하므로 보수적으로 0점 처리
- [x] `BidTypeRule` 구현 (+10) — `preferred_bid_types`/`preferred_contract_types`와 `bid_type`/`contract_type` 일치 여부, 각 5점씩 부분 인정. 선호 미설정/공고 값 없음은 해당 절반 만점 처리
- [x] `DeadlineRule` 구현 (+10) — `bid_deadline` 기준 `deadline_min_days` 대비 여유일 3단계 채점(여유 충분 10점 / 절반 이상 5점 / 미만 0점). `bid_deadline`/`deadline_min_days` 없으면 만점 처리
- [x] `RegionRestrictionRule` 구현 (+5) — `region_restriction` 없음 여부
- [x] `MatchingEngine` 구현 — 등록된 모든 `ScoreRule`을 실행해 총점 합산, 등급 산정, `score_reason` 조합. `matched_keywords`는 4개 세부 컬럼 유무와 무관하게 8개 룰 전부의 구조화된 결과 배열로 저장
- [x] `BidMatchResultRepository` 구현 — `(bid_notice_id, company_id)` 기준 upsert
- [x] `MatchCalculationService` 구현 — 공고 1건 + 회사 1건을 받아 `MatchingEngine` 실행 후 `bid_match_results` 저장/갱신
- [x] 결정론성 검증 테스트 작성 — 동일 입력 반복 호출 시 항상 동일한 점수/등급이 나오는지 확인
