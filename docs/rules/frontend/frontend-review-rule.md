# Frontend Review Rule

## 코드 완료 후 체크리스트

### HTTP / API

- [ ] `axios.get()` / `axios.post()` 직접 호출이 없다 (항상 인스턴스 사용)
- [ ] `withCredentials: true`가 axios 인스턴스에 설정되어 있다
- [ ] CSRF 헤더 (`X-XSRF-TOKEN`)가 interceptor에서 자동으로 추가된다
- [ ] API 함수는 `src/api/` 에 있고, 훅 내부에 fetch 로직이 직접 작성되지 않았다
- [ ] Response wrapper (`{ header, data }`)가 interceptor에서 unwrap된다

### 컴포넌트

- [ ] Page 컴포넌트에서 직접 `useQuery`를 호출하지 않는다 (도메인 훅으로 감쌌다)
- [ ] 컴포넌트 props에 `any` 타입이 없다
- [ ] 로딩 / 에러 / 빈 상태 UI가 모두 구현되어 있다
- [ ] 서버 상태는 React Query, UI 상태는 `useState`로 관리된다

### 타입

- [ ] 백엔드 DTO에 대응하는 타입이 `src/types/`에 정의되어 있다
- [ ] API 함수 반환 타입이 명시되어 있다
- [ ] `any`가 없다

### 스타일

- [ ] CSS 파일을 새로 만들지 않았다 (Tailwind 클래스만 사용)
- [ ] 색상 기준(적합도 점수별)이 frontend-rule.md §8 기준을 따른다

### 환경변수

- [ ] `VITE_` 접두사가 붙어 있다
- [ ] `.env.example`에 변수 목록이 업데이트되어 있다
