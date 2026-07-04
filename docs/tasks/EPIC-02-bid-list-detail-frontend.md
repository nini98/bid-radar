# EPIC-02 공고 목록/상세 화면에 적합도 결과 반영 (Frontend)

## 목표

공고 목록 화면(`BidCard`, `BidListPage`)에 적합도 점수·등급 배지와 `grade` 필터를 반영한다. 아직 구현되지 않은 공고 상세 화면(`01. 화면기획 > 공고 상세 화면` 설계 기준)의 기본 골격을 만들고, 그중 적합도 섹션을 실제 데이터로 노출한다. AI 요약/위험 요인/유사 공고 섹션은 Epic-3 데이터가 필요하므로 이번 태스크에서는 placeholder로만 남긴다.

---

## 파트

Frontend

---

## 참조 Rule

- `docs/rules/frontend/frontend-rule.md`

---

## Done Condition

- [ ] 공고 카드에 적합도 점수와 등급 배지가 표시된다 (80점↑ 초록 / 60~79점 주황 / 60점↓ 회색)
- [ ] 회사 프로필이 없는 사용자에게는 배지 대신 "회사 프로필을 설정하면 적합도를 확인할 수 있습니다" 안내가 표시된다
- [ ] `grade` 필터 UI가 추가되고 선택 시 목록이 필터링된다
- [ ] 정렬 옵션에 점수순(`score`)이 실제로 동작한다
- [ ] 공고 카드 또는 상세보기 버튼 클릭 시 공고 상세 화면(`/bids/:bidId`)으로 이동한다
- [ ] 상세 화면에 공고 기본 정보(공고명, 기관, 예산, 지역, 입찰 방식, 자격요건 요약)가 표시된다
- [ ] 상세 화면에 적합도 섹션(총점, 등급, 세부 점수, 추천 사유)이 표시된다
- [ ] 상세 화면에 주요 일정(질의/서류/입찰 마감, 개찰일)이 표시된다
- [ ] 상세 화면에 첨부파일 목록과 다운로드 링크가 표시된다
- [ ] 상세 화면의 Loading / Error / Not Found 상태가 각각 표시된다
- [ ] AI 요약, 위험 요인, 유사 공고 영역은 "분석 준비 중"과 같은 placeholder로 표시된다

---

## Out of Scope

- AI 요약 / 위험 요인 / 유사 공고 실제 데이터 연동 → Epic-3
- 즐겨찾기 등록/해제 버튼 → 별도 확인 후 필요 시 후속 Task
- 회사 프로필 설정 화면 → `EPIC-02-company-profile-frontend`

---

## 작업 항목

### 목록 화면
- [ ] `BidNoticeSummary` 타입에 `matchResult` 필드 추가 (totalScore, grade, displayText)
- [ ] `BidSearchParams` 타입에 `grade` 필드 추가
- [ ] `MatchBadge` 컴포넌트 구현 (등급별 색상, `matchResult`가 없으면 "미설정" 표시)
- [ ] `BidCard`에 `MatchBadge` 추가, 상세 화면 라우팅 연결
- [ ] 필터 컴포넌트에 `grade` 선택 UI 추가 (전체 / 적극 검토 / 추천 / 검토 필요)
- [ ] `useBidList` 훅에 `grade` 파라미터 반영
- [ ] 회사 프로필 미설정 안내 배너 추가 (`GET /api/companies/me` 응답이 없을 때 `BidListPage` 상단에 노출)

### 상세 화면 (신규)
- [ ] `BidNoticeDetail`, `MatchResult` 프론트 타입 정의 (03 API 명세 응답 구조 기준)
- [ ] 라우팅 등록 (`/bids/:bidId`)
- [ ] `useBidDetail` 훅 구현 (`GET /api/bids/{bidId}`)
- [ ] `BidDetailPage` 컴포넌트 구현
  - 공고 기본 정보 섹션
  - 적합도 섹션 (총점/등급/세부 점수/추천 사유, `matchResult` 없으면 미설정 안내)
  - 일정 섹션 (질의/서류/입찰 마감, 개찰일)
  - 첨부파일 섹션 (목록 + 다운로드 링크)
  - AI 요약 / 위험 요인 / 유사 공고 섹션 — "분석 준비 중" placeholder
- [ ] Loading(스켈레톤) / Error(재시도) / Not Found 상태 UI 구현
- [ ] 첨부파일 다운로드 링크 연결 (`GET /api/bids/{bidId}/attachments/{attachmentId}/download`)
