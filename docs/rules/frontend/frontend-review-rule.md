# Frontend Review Rule

## 코드 완료 후 체크리스트

### HTTP / API

- [ ] `axios.get()` / `axios.post()` 직접 호출이 없다 (항상 인스턴스 사용)
- [ ] `withCredentials: true`가 axios 인스턴스에 설정되어 있다
- [ ] CSRF 헤더 (`X-XSRF-TOKEN`)가 interceptor에서 자동으로 추가된다
- [ ] API 함수는 `src/api/` 에 있고, 훅 내부에 fetch 로직이 직접 작성되지 않았다
- [ ] Response wrapper (`{ header, data }`)가 interceptor에서 unwrap된다
- [ ] 에러 응답 처리 시 `data`(필드 상세 메시지)를 우선 사용하고 `header.resultMessage`로 폴백한다

### 컴포넌트

- [ ] Page 컴포넌트에서 직접 `useQuery`를 호출하지 않는다 (도메인 훅으로 감쌌다)
- [ ] 컴포넌트 props에 `any` 타입이 없다
- [ ] 로딩 / 에러 / 빈 상태 UI가 모두 구현되어 있다
- [ ] 여러 쿼리 훅을 조합해 쓰는 경우, 모든 훅의 `isError`가 분기에 반영되어 있다
- [ ] 서버 상태는 React Query, UI 상태는 `useState`로 관리된다
- [ ] 로그인 사용자 개인 데이터("내 정보") 쿼리는 queryKey에 사용자 id가 포함되어 있다

### 타입

- [ ] 백엔드 DTO에 대응하는 타입이 `src/types/`에 정의되어 있다
- [ ] API 함수 반환 타입이 명시되어 있다
- [ ] `any`가 없다

### 스타일

- [ ] CSS 파일을 새로 만들지 않았다 (Tailwind 클래스만 사용)
- [ ] 색상 기준(적합도 점수별)이 frontend-rule.md §8 기준을 따른다
- [ ] sticky/fixed 헤더가 있는 화면이라면 폼 요소에 `scroll-mt-*`가 적용되어 있다

### 환경변수

- [ ] `VITE_` 접두사가 붙어 있다
- [ ] `.env.example`에 변수 목록이 업데이트되어 있다

### 검증

- [ ] 실제 브라우저(Playwright MCP 등)로 골든 패스와 주요 에러 상태(검증 실패, API 실패)를 직접 확인했다 — 타입체크/린트로 안 잡히는 렌더링·포커스·레이아웃 버그가 있다
