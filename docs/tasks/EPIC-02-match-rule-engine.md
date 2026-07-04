# EPIC-02 적합도 Rule Engine

## 목표

회사 프로필과 공고 정보를 비교해 룰 기반으로 적합도 점수(0~100점)와 등급을 계산하는 Rule Engine을 구현한다. AI 분석 없이 `bid_notices` 필드와 회사 프로필만으로 동작해야 한다.

---

## 파트

Spring Boot

---

## 참조 Rule

- `docs/rules/springboot/architecture-rule.md`
- `docs/rules/springboot/repository-rule.md`
- `docs/rules/springboot/transaction-rule.md`
- `docs/rules/springboot/testing-rule.md`

---

## Done Condition

- [ ] 회사 프로필 + 공고 정보를 입력하면 0~100점 사이 총점이 계산된다
- [ ] 동일한 입력에 대해 항상 동일한 점수가 계산된다 (결정론성)
- [ ] 총점 80점 이상 `STRONG_REVIEW`, 60~79점 `RECOMMENDED`, 60점 미만 `NEED_REVIEW` 등급이 부여된다
- [ ] `score_tech`, `score_region`, `score_budget`, `score_business` 세부 점수가 `bid_match_results`에 저장된다
- [ ] `score_reason`에 점수 계산 근거가 텍스트로 기록된다
- [ ] 새 `ScoreRule` 구현체를 추가하는 것만으로 점수 정책을 확장할 수 있는 구조다

---

## Out of Scope

- AI 분석 결과에 의존하는 룰(자격요건 충족, 공동수급 가능 여부, 리스크 감점) → Epic-3
- 매칭 계산을 언제 실행할지(수집 후 자동 계산, 재계산 API) → `EPIC-02-match-recalculate`
- 매칭 결과를 공고 목록/상세 API 응답에 노출 → `EPIC-02-bid-list-match-integration`

---

## 작업 항목

- [ ] `ScoreRule` 인터페이스 정의 — `ScoreResult calculate(BidNotice bid, Company company)`
- [ ] `ScoreResult` 값 객체 작성 (score, reason, matchedKeywords)
- [ ] `TechTagMatchRule` 구현 (+20) — 보유 기술 태그와 공고명/자격요건 요약 텍스트 일치 여부
- [ ] `BusinessAreaMatchRule` 구현 (+20) — 보유 사업 분야와 공고 조달분류/공고명 일치 여부
- [ ] `BudgetRangeRule` 구현 (+15) — `company_bid_preferences.budget_min/budget_max` 범위 기준 (범위 내 만점, 초과 시 부분 점수, 크게 벗어나면 0점)
- [ ] `RegionMatchRule` 구현 (+10) — `preferred_regions` vs `bid_notices.region`
- [ ] `IndustryRestrictionRule` 구현 (+10) — `industry_restriction` 없음 또는 회사 자격과 부합 시 만점
- [ ] `BidTypeRule` 구현 (+10) — `preferred_bid_types`/`preferred_contract_types`와 `bid_type`/`contract_type` 일치 여부
- [ ] `DeadlineRule` 구현 (+10) — `deadline_min_days` 기준 마감 여유일 평가
- [ ] `RegionRestrictionRule` 구현 (+5) — `region_restriction` 없음 여부
- [ ] `MatchingEngine` 구현 — 등록된 모든 `ScoreRule`을 실행해 총점 합산, 등급 산정, `score_reason` 조합
- [ ] `BidMatchResultRepository` 구현 — `(bid_notice_id, company_id)` 기준 upsert
- [ ] `MatchCalculationService` 구현 — 공고 1건 + 회사 1건을 받아 `MatchingEngine` 실행 후 `bid_match_results` 저장/갱신
- [ ] 결정론성 검증 테스트 작성 — 동일 입력 반복 호출 시 항상 동일한 점수/등급이 나오는지 확인
