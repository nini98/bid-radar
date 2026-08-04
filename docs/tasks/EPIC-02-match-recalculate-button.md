# EPIC-02 프로필 화면 상시 재계산 버튼

## 목표

회사 프로필 화면에 매칭 계산 상태를 보여주는 상시 버튼을 추가하고, 더 이상 존재하지 않는 구 재계산 API 호출을 프론트에서 제거한다.

---

## 파트

frontend

---

## 관련 Issue

- Issue: #33

담당 Next Step (생성 시점 원문 그대로 인용, Issue 본문과 동일하게 들여쓰기 없이 기록):
- [x] 프론트: 프로필 화면에 상시 재계산 버튼 추가, 상태별(FAILED/IN_PROGRESS 5분 이내·초과/DONE) 활성화 여부와 문구 표시, IN_PROGRESS 동안 상태 재조회로 화면 갱신, DONE 시 캐시 무효화

---

## 참조 Rule

- `docs/rules/frontend/frontend-rule.md`
- `docs/rules/frontend/testing-rule.md`

---

## Done Condition

- [x] 프로필 화면에 매칭 계산 상태에 따라 아래 표대로 버튼 활성화 여부와 문구가 표시된다

  | 상태 | 버튼 | 문구 |
  |---|---|---|
  | `FAILED` | 활성화 | "재계산에 실패했습니다." |
  | `IN_PROGRESS` (updatedAt 기준 5분 이내) | 비활성화 | "재계산 중입니다." |
  | `IN_PROGRESS` (updatedAt 기준 5분 초과) | 활성화 | "재계산이 예상보다 오래 걸리고 있습니다. 재계산을 다시 시작해 주세요." |
  | `DONE` | 비활성화 | 완료 상태로 표시 |
  | 이력 없음(`status: null`) | 비활성화 | 미계산 상태로 표시 |

- [x] 버튼 클릭 시 `POST /api/companies/me/match-status/retry`를 호출한다. "5분 경과" 판단은 표시용일 뿐이며 최종 허용 여부는 항상 서버가 판단하므로, 클릭이 서버에서 거부(409)되면 서버가 내려준 에러 메시지를 토스트로 표시한다
- [x] `IN_PROGRESS`인 동안 짧은 간격으로 `GET /api/companies/me/match-status`를 재조회해 화면을 갱신한다 (별도 타임아웃 개념 없음 — 상태가 바뀔 때까지 계속 조회)
- [x] `DONE`으로 전이되면 별도 알림 없이 관련 쿼리 캐시(`['bids']`)를 무효화한다
- [x] `useSaveCompanyProfile`이 더 이상 존재하지 않는 `POST /companies/me/recalculate`를 호출하지 않는다 (재계산은 백엔드가 프로필 저장 시 도메인 이벤트로 자동 트리거하므로, 프론트는 상태를 조회만 한다)

---

## Out of Scope

- 개별 공고 단위 계산 실패 상태 표현 (Issue #40)
- 프로필 저장(`PUT /companies/me`) 자체가 진행 중인 재계산으로 인해 409(`MATCH_CALCULATION_IN_PROGRESS`)로 거부되는 경우의 전용 UX 문구 — 기존 `err.message` 기반 범용 에러 토스트로 이미 서버 메시지가 노출되므로 추가 작업 없음
- 폴링 간격/락 유효기간을 프론트가 재정의하는 것 — 클라이언트는 표시용 판단만 하고, 최종 허용 여부는 항상 서버 CAS 쿼리가 결정한다 (Issue #33 설계 그대로)

---

## 작업 항목

- [x] `src/types/match.ts`: `MatchCalculationStatus` 타입 정의 (`status: 'IN_PROGRESS' | 'DONE' | 'FAILED' | null`, `updatedAt: string | null`)
- [x] `src/api/match.ts`: `fetchMatchStatus()` (`GET /companies/me/match-status`), `retryMatchStatus()` (`POST /companies/me/match-status/retry`) 추가
- [x] `src/api/company.ts`, `src/hooks/useCompanyProfile.ts`: `recalculateMatch()` 및 그 호출부 제거, 저장 성공 토스트 문구를 재계산 언급 없이 단순화
- [x] `src/hooks/useMatchStatus.ts`: `useMatchStatus()` 쿼리(`queryKey: ['match-status', 'me', user?.id]` — `company` 쿼리와 별개 네임스페이스로 분리, `IN_PROGRESS`일 때만 `refetchInterval` 활성화), `useRetryMatchStatus()` 뮤테이션
- [x] `DONE` 전이 감지 시 `['bids']` 쿼리 무효화 연결
- [x] `src/components/company/MatchStatusButton.tsx`: 상태별 활성화 여부/문구 렌더링, 클릭 시 재시도 호출
- [x] `CompanyProfilePage.tsx`에 `MatchStatusButton` 배치
- [x] 단위 테스트: 상태값 → 버튼 활성화 여부/문구 매핑 로직 대상 테스트 작성 (5분 경계 포함)
