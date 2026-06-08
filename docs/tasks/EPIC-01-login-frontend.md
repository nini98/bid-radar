# EPIC-01 로그인 / 회원가입 화면 (Frontend)

## 목표

로그인 화면과 회원가입 화면을 구현한다. 인증 후 공고 목록 화면으로 이동하고, 비인증 접근 시 로그인 화면으로 리다이렉트한다.

---

## 파트

Frontend

---

## 참조 Rule

- `docs/rules/frontend/frontend-rule.md` ← 작업 전 생성 필요

---

## Done Condition

- [ ] 로그인 화면이 브라우저에서 정상 렌더링된다
- [ ] 이메일 / 비밀번호 입력 후 로그인 성공 시 공고 목록 화면으로 이동한다
- [ ] 로그인 실패 시 에러 메시지가 표시된다
- [ ] 회원가입 화면에서 가입 성공 시 로그인 화면으로 이동한다
- [ ] 인증되지 않은 상태에서 공고 목록 접근 시 로그인 화면으로 리다이렉트된다

---

## Out of Scope

- 소셜 로그인
- 이메일 인증
- 비밀번호 찾기

---

## 작업 항목

- [ ] 로그인 페이지 컴포넌트 구현 (`LoginPage`)
- [ ] 회원가입 페이지 컴포넌트 구현 (`SignupPage`)
- [ ] 로그인 / 회원가입 API 훅 구현 (`useLogin`, `useSignup`)
- [ ] 인증 상태 관리 구현 (로그인 여부, 사용자 정보)
  - 앱 초기 진입 시 `GET /api/auth/me` 호출로 세션 복원
- [ ] PrivateRoute 구현 (비인증 접근 시 로그인 화면으로 리다이렉트)
- [ ] React Router 라우트 구성
  - `/login` → LoginPage
  - `/signup` → SignupPage
  - `/` → BidListPage (PrivateRoute)
- [ ] 로그인 / 회원가입 폼 유효성 검사 (이메일 형식, 비밀번호 길이)
