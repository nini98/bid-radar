# Frontend Rule

## 1. 문서 목적

이 문서는 React 프론트엔드 개발 기준을 정의한다.
컴포넌트 구조, API 연동, 상태 관리, 스타일 방침을 다룬다.

---

## 2. 기술 스택

| 항목 | 선택 |
|---|---|
| 언어 | TypeScript |
| 번들러 | Vite |
| UI 프레임워크 | React 18 |
| 스타일 | Tailwind CSS |
| 서버 상태 | TanStack Query (React Query) v5 |
| 라우팅 | React Router v6 |
| HTTP | Axios |

---

## 3. 디렉토리 구조

```
src/
├── api/          # axios API 함수 (도메인별 파일)
├── components/   # 재사용 UI 컴포넌트
│   ├── bid/      # 공고 도메인 컴포넌트
│   └── common/   # 공통 컴포넌트 (Button, Pagination 등)
├── hooks/        # React Query 훅 (useBidList, useBidDetail 등)
├── lib/          # axios 인스턴스, queryClient 설정
├── pages/        # 페이지 컴포넌트 (라우트 1:1 대응)
├── types/        # TypeScript 타입 (백엔드 DTO 대응)
└── App.tsx
```

---

## 4. HTTP 클라이언트 규칙

### 4-1. axios 인스턴스

axios 인스턴스는 `src/lib/axios.ts` 한 곳에서만 생성한다.
직접 `axios.get()` 호출은 금지한다. 항상 인스턴스를 통해 호출한다.

필수 설정:
- `baseURL`: 환경 변수 `VITE_API_BASE_URL`
- `withCredentials: true` — HttpOnly 쿠키 전송 필수
- Request interceptor: `XSRF-TOKEN` 쿠키를 읽어 `X-XSRF-TOKEN` 헤더에 자동 추가

```ts
// 예시 패턴
const instance = axios.create({ baseURL, withCredentials: true });
instance.interceptors.request.use((config) => {
  const xsrf = getCookie('XSRF-TOKEN');
  if (xsrf) config.headers['X-XSRF-TOKEN'] = xsrf;
  return config;
});
```

### 4-2. 공통 Response Wrapper 처리

백엔드 응답 구조: `{ header: { resultCode, resultMessage }, data }`

Response interceptor에서 `data.data`를 unwrap하여 API 함수는 실제 데이터만 반환한다.
`resultCode !== '200'`이면 에러로 처리한다.

```ts
instance.interceptors.response.use((res) => {
  const body = res.data;
  if (body.header.resultCode !== '200') throw new Error(body.header.resultMessage);
  return body.data;
});
```

### 4-3. API 함수 위치

- `src/api/{domain}.ts` 형태로 도메인별 분리
- API 함수는 순수 함수로 작성 (훅 내부에 fetch 로직 직접 작성 금지)

---

## 5. 서버 상태 관리 (React Query)

### 5-1. QueryKey 규칙

queryKey는 도메인 + 파라미터를 배열로 구성한다.

```ts
['bids', params]       // 목록
['bids', bidId]        // 단건
['auth', 'me']         // 인증
```

### 5-2. 기본 설정

`QueryClient`는 `src/lib/queryClient.ts`에 한 곳에서 생성한다.

권장 기본값:
- `staleTime: 1000 * 60` — 1분
- `retry: 1`
- 401 에러는 retry하지 않는다

### 5-3. 훅 위치

React Query 훅은 `src/hooks/` 에 둔다.
Page 컴포넌트에서 직접 `useQuery`를 호출하지 않는다. 항상 도메인 훅으로 감싼다.

---

## 6. 컴포넌트 설계 원칙

### 6-1. Page vs Component 분리

- **Page**: `src/pages/`에 위치. 데이터 페칭 훅 호출과 전체 레이아웃 조합 담당.
- **Component**: `src/components/`에 위치. 재사용 가능한 UI 단위. props로 데이터를 받는다.

Page 컴포넌트는 직접 UI 스타일을 작성하지 않는다. 컴포넌트를 조합한다.

### 6-2. 상태 처리 위치

- 서버 상태: React Query 훅
- UI 상태(열림/닫힘, 탭 선택): `useState`를 컴포넌트 내부에서 관리
- 전역 클라이언트 상태는 최소화한다. 필요 시 Context API를 사용한다

### 6-3. props 전달 규칙

- 컴포넌트는 필요한 데이터만 props로 받는다
- 훅이나 전역 상태를 컴포넌트 내부에서 직접 호출하면 재사용성이 떨어진다
- 단, 화면 규모가 작아 prop drilling이 의미 없는 경우는 예외

---

## 7. TypeScript 타입 규칙

- 백엔드 DTO에 대응하는 인터페이스/타입은 `src/types/` 에 정의한다
- `any`를 사용하지 않는다
- API 함수의 반환 타입은 명시한다

```ts
// src/types/bid.ts
export interface BidNoticeSummary {
  id: number;
  title: string;
  agency: string | null;
  budget: number | null;
  region: string | null;
  bidType: string | null;
  status: 'OPEN' | 'CLOSED' | 'CANCELED';
  bidDeadline: string | null;
  publishedAt: string | null;
}
```

---

## 8. 스타일 규칙

- 스타일은 Tailwind CSS 유틸리티 클래스로만 작성한다
- 별도 CSS 파일을 새로 만들지 않는다 (`index.css`의 Tailwind directives 제외)
- 반복되는 클래스 조합은 컴포넌트로 추출한다 (CSS 변수 / 커스텀 클래스 금지)
- 색상 기준:
  - 적합도 80점↑: `text-green-600` / `bg-green-50`
  - 적합도 60~79점: `text-orange-500` / `bg-orange-50`
  - 적합도 60점↓ / 미분석: `text-gray-400` / `bg-gray-50`

---

## 9. 에러 및 로딩 상태 원칙

- 로딩 상태: 스켈레톤 UI 또는 로딩 스피너로 표시한다
- 에러 상태: 에러 메시지와 재시도 버튼을 함께 표시한다
- 빈 상태: "검색 결과가 없습니다" 등 명확한 안내 UI를 표시한다
- React Query의 `isLoading`, `isError`, `data` 상태를 순서대로 분기 처리한다

---

## 10. 환경변수 규칙

- `VITE_` 접두사를 반드시 붙인다
- `.env.local` 파일에 로컬 설정을 둔다 (`.gitignore` 대상)
- `.env.example` 파일로 필요한 변수 목록을 공유한다

---

## 11. 한 줄 요약

- 타입은 `types/`에, API 함수는 `api/`에, 훅은 `hooks/`에, 컴포넌트는 `components/`에 둔다
- Page는 조합하고, Component는 재사용한다
- HTTP는 반드시 axios 인스턴스를 통한다. CSRF 헤더와 credentials는 자동으로 처리한다
- 서버 상태는 React Query로, UI 상태는 useState로 관리한다
