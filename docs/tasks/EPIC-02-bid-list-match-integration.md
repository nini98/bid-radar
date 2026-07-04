# EPIC-02 공고 목록/상세 API에 매칭 결과 통합

## 목표

`EPIC-01-bid-list-api`에서 의도적으로 제외했던 `matchResult`, `grade` 필터를 공고 목록/상세 API에 반영한다.

---

## 파트

Spring Boot

---

## 참조 Rule

- `docs/rules/springboot/architecture-rule.md`
- `docs/rules/springboot/repository-rule.md`
- `docs/rules/springboot/dto-response-rule.md`
- `docs/rules/springboot/api-response-rule.md`

---

## Done Condition

- [ ] `GET /api/bids` 응답의 각 카드에 `matchResult`(totalScore, grade, displayText)가 포함된다
- [ ] `grade` 필터(`STRONG_REVIEW`/`RECOMMENDED`/`NEED_REVIEW`)가 동작한다
- [ ] `sort=score` 정렬이 실제 `bid_match_results.total_score` 기준으로 동작한다
- [ ] 로그인한 사용자의 회사 프로필이 없으면 `matchResult`가 `null`로 반환되고 목록 조회는 정상 동작한다
- [ ] `GET /api/bids/{bidId}` 응답에 `matchResult`(totalScore, grade, displayText, scoreTech, scoreBusiness, scoreBudget, scoreRegion, matchedKeywords, scoreReason)가 포함된다
- [ ] 회사 프로필이 없는 사용자의 상세 조회도 에러 없이 동작한다 (`matchResult` null)

---

## Out of Scope

- `riskSummary`(AI 리스크 요약) → Epic-3
- 즐겨찾기 관련 로직 변경

---

## 작업 항목

- [ ] `BidNoticeSearchCondition`의 `grade` 필터 필드 활성화 (Epic-1에서 보류했던 부분)
- [ ] `BidNoticeRepositoryImpl` 쿼리에 `bid_match_results` LEFT JOIN 추가 (로그인 사용자의 `company_id` 기준, `grade` 필터, `sort=score` 반영)
- [ ] `MatchResultResponse` DTO 작성 (totalScore, grade, displayText, scoreTech, scoreBusiness, scoreBudget, scoreRegion, matchedKeywords, scoreReason)
- [ ] `BidNoticeSummaryResponse`에 `matchResult` 필드 추가
- [ ] `BidNoticeDetailResponse`에 `matchResult` 필드 추가
- [ ] `BidNoticeService` 수정 — 로그인 사용자의 `companyId` 조회 후 목록/상세 조회에 함께 전달, 회사 없으면 `matchResult` `null` 처리
- [ ] `grade` → `displayText` 매핑 유틸 구현 (`STRONG_REVIEW`→"적극 검토", `RECOMMENDED`→"추천", `NEED_REVIEW`→"검토 필요")
