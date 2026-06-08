# EPIC-01 공고 목록 / 상세 API

## 목표

공고 목록 조회 API와 공고 상세 조회 API를 구현한다. 키워드 검색, 필터, 정렬, 페이지네이션을 지원한다.

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
- `docs/rules/springboot/exception-rule.md`
- `docs/rules/springboot/transaction-rule.md`

---

## Done Condition

- [ ] `GET /api/bids` 요청 시 공고 목록이 페이지네이션으로 반환된다
- [ ] keyword, region, budgetMin, budgetMax, deadlineDays 필터가 동작한다
- [ ] sort=score, deadline, latest 정렬이 동작한다
- [ ] 응답에 summary(todayNewCount, todayRecommendedCount, todayDeadlineCount)가 포함된다
- [ ] `GET /api/bids/{bidId}` 요청 시 공고 상세가 반환된다
- [ ] 존재하지 않는 bidId 요청 시 404 응답이 반환된다
- [ ] 모든 응답이 공통 Wrapper를 사용한다

---

## Out of Scope

- 적합도 점수 (matchResult) → Epic-2 (이번 Task에서는 응답에서 생략)
- grade 필터 → Epic-2 (bid_match_results 테이블이 없으므로 이번 Task에서는 제외)
- AI 분석 결과 (analysis) → Epic-3 (이번 Task에서는 응답에서 생략)
- 즐겨찾기 등록/해제 API → `EPIC-01-bid-list-frontend` 작업 시 함께 구현

---

## 작업 항목

- [ ] `BidNoticeSearchCondition` DTO 작성 (keyword, region, grade, budgetMin, budgetMax, deadlineDays, sort, page, size)
- [ ] `BidNoticeSummaryResponse` DTO 작성 (목록 카드용)
- [ ] `BidNoticeDetailResponse` DTO 작성 (상세용, bid + attachments 포함)
- [ ] `BidListResponse` DTO 작성 (summary + content + 페이지네이션)
- [ ] `BidNoticeRepositoryImpl` 구현 (QueryDSL)
  - 동적 필터 조건 (keyword, region, grade, 예산 범위, 마감 여유일)
  - 정렬 (score, deadline, latest)
  - DTO Projection (엔티티 전체 로딩 금지)
- [ ] `BidNoticeService` 구현
  - 목록 조회 + summary 계산
  - 상세 조회 (없으면 ApiException NOT_FOUND)
- [ ] `BidNoticeController` 구현
  - `GET /api/bids`
  - `GET /api/bids/{bidId}`
