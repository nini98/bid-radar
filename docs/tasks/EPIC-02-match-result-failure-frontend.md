# EPIC-02 매칭 계산 실패 상태 프론트 노출

## 목표

`BidMatchResult.status`(SUCCESS/FAILED)를 API 응답 계약에 실제로 노출하고, 프론트에서 "계산 실패" 상태를 "미계산"과 구분해서 보여준다.

## 파트

backend + frontend

## 관련 Issue

- Issue: #40

담당 Next Step (생성 시점 원문 그대로 인용):
- [x] 프론트에서 "계산 실패" 상태 노출 방식 결정 및 구현 (backend 응답 계약 변경 포함 — 위 제안 방향 마지막 항목 참고)

## 참조 Rule

- docs/rules/springboot/dto-response-rule.md
- docs/rules/springboot/mapper-rule.md
- docs/rules/springboot/testing-rule.md
- docs/rules/frontend/frontend-rule.md
- docs/rules/frontend/testing-rule.md

## 불변조건

1. `matchResult`가 `null`이면 "계산 시도 자체가 없음(미계산)"만을 의미한다. non-null이면 반드시 `status`(SUCCESS/FAILED)를 가지며, "없음 / 성공 / 실패" 세 상태는 응답만 보고 항상 구분 가능해야 한다.
2. `status=SUCCESS`일 때만 `totalScore`/`grade`/`displayText`가 채워지고, `status=FAILED`일 때는 이 필드들이 전부 `null`이다 — DB CHECK 제약(V16)이 이미 강제하는 계약을 API 응답도 그대로 따른다.
3. 이번 Task는 실패 상태를 "노출"만 한다 — 실패 건 자동 재시도나 새 재계산 트리거는 추가하지 않는다.
4. `matchResult`가 `null`일 때(미계산) 기존 동작·문구는 그대로 유지된다 — FAILED 케이스만 새 분기로 추가한다.
5. 목록 응답과 상세 응답은 동일한 상태 구분 규칙을 따른다.
6. `grade` 필터/`SCORE` 정렬 등 기존 쿼리 동작은 FAILED row에 대해 바뀌지 않는다.

## 결정 목록

- **A**: "존재 여부" 판단 신호를 `totalScore` null-check → `status` null-check로 변경. 이유: `totalScore`는 FAILED일 때도 null이라 구분 불가.
- **B**: `MatchResultSummaryResponse`/`MatchResultResponse`에 `status: BidMatchResultStatus` 필드 추가(기존 도메인 enum 재사용).
- **C**: FAILED 뱃지 label "계산 실패", 스타일 `bg-red-50 text-red-500` (초록/주황/회색과 겹치지 않는 색).
- **D**: `BidMatchSection`의 FAILED는 브레이크다운/scoreReason 생략, 안내 문구("이 공고의 적합도 계산에 실패했습니다. 회사 프로필을 다시 저장하면 재계산을 시도할 수 있습니다.") + 기존 `/company/profile` 링크 재사용. 새 재시도 버튼은 만들지 않음(Out of Scope).
- **E**: `BidCard` 등 `MatchBadge` 재사용처는 별도 처리 없이 자동 반영.

## 시나리오

| 상태 | row | API `matchResult` | 목록 뱃지 | 상세 섹션 |
|---|---|---|---|---|
| 미계산 | 없음 | `null` | "미설정"(회색) | 프로필 설정 안내 (기존) |
| 성공 | SUCCESS | status=SUCCESS + 점수 | 점수 기반 색상 (기존) | 브레이크다운 + scoreReason (기존) |
| 실패 | FAILED | status=FAILED, 점수 전부 null | "계산 실패"(빨강, 신규) | 안내 문구 + 링크 (신규) |
| 실패 후 재계산 성공 | SUCCESS로 갱신 | status=SUCCESS | 정상 뱃지로 자연 복귀 | 정상 그리드로 자연 복귀 |

## Done Condition

- [x] `MatchResultSummaryResponse`/`MatchResultResponse`에 `status` 필드가 추가되고, "미계산"과 "FAILED"가 응답만으로 구분 가능하다
- [x] `BidNoticeRepositoryImpl`의 QueryDSL 프로젝션이 `status`를 조회하고, `BidNoticeSummaryResponse`의 정규화 기준이 `status==null`이다
- [x] `BidNoticeService.getDetail()`이 FAILED 결과를 더 이상 숨기지 않고 그대로 반환한다
- [x] 목록 카드/상세 페이지 뱃지가 FAILED일 때 "계산 실패"(빨강)를 표시한다
- [x] `BidMatchSection`이 FAILED일 때 브레이크다운/scoreReason 대신 안내 문구 + 프로필 링크를 렌더링한다
- [x] "테스트가 증명해야 할 것" 1~6번이 전부 작성된다
- [x] `docs/qa/EPIC-02-bid-list-detail.md`에 FAILED 시나리오가 추가된다

## Out of Scope

- 실패 건 자동 재시도, 새 재시도 버튼/API 추가
- `MatchStatusButton`(회사 전체 재계산 버튼) 로직 변경
- Issue #51/#53 (리스너 스레드풀 포화, 동시 갱신 경쟁) — 별도 Issue

## 테스트가 증명해야 할 것

1. 매칭 row가 없으면(LEFT JOIN 미스) 목록 응답의 `matchResult`가 `null`이다 — 통합, `BidNoticeRepositoryImplTest`
2. 매칭 row가 SUCCESS면 목록 응답의 `matchResult`에 `status=SUCCESS`와 점수/등급이 채워진다 — 통합, `BidNoticeRepositoryImplTest`
3. 매칭 row가 FAILED면 목록 응답의 `matchResult`가 숨겨지지 않고 `status=FAILED`, 점수/등급은 `null`이다 — 통합, `BidNoticeRepositoryImplTest`
4. 상세 조회 시 FAILED 결과가 숨겨지지 않고 `status=FAILED`로 그대로 내려간다 — `BidNoticeServiceTest`
5. 상세 조회 시 SUCCESS 결과는 기존과 동일하게 점수 포함해서 내려간다 — `BidNoticeServiceTest`
6. `matchBadge.ts`의 상태→라벨/스타일 매핑이 FAILED면 "계산 실패"/빨강, SUCCESS면 기존 점수 라벨을 반환한다 — 단위, `matchBadge.test.ts`

테스트 생략: `MatchBadge`/`BidMatchSection`의 FAILED 분기 렌더링 — presentational 컴포넌트(frontend testing-rule §4-2), 로직은 6번 pure function에 있음. `docs/qa/EPIC-02-bid-list-detail.md`에 시나리오 추가 후 Playwright MCP로 자체점검 시 확인.

## 작업 항목

- [x] `MatchResultSummaryResponse`에 `status` 필드 추가, compact ctor 정규화 기준 변경
- [x] `MatchResultResponse`에 `status` 필드 추가
- [x] `BidNoticeSummaryResponse` compact ctor 정규화 기준 변경
- [x] `BidNoticeRepositoryImpl` QueryDSL 프로젝션에 `matchResult.status` 추가
- [x] `BidNoticeService.getDetail()` FAILED 숨김 로직 제거
- [x] `BidNoticeMapper` 매핑 확인 (자동 매핑 여부 검증) — 프로퍼티명 일치로 자동 매핑됨, 별도 수정 불필요
- [x] 위 테스트 1~5번 작성/갱신
- [x] `frontend/src/types/bid.ts`에 `MatchResultStatus` 타입 및 `status` 필드 추가, nullable 조정
- [x] `frontend/src/lib/matchBadge.ts`에 FAILED 라벨/스타일 매핑 추가
- [x] 테스트 6번 작성
- [x] `MatchBadge.tsx`가 `matchBadge.ts` 결과를 사용하도록 수정
- [x] `BidMatchSection.tsx`에 FAILED 분기 추가
- [x] `docs/qa/EPIC-02-bid-list-detail.md`에 FAILED 시나리오 추가 및 Playwright MCP 확인
