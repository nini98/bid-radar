# EPIC-01 인증 구조 구현 (JWT Cookie)

## 목표

JWT Cookie 기반 인증 구조를 구현한다. 회원가입, 로그인, 토큰 갱신, 내 정보 조회 API를 제공한다.

---

## 파트

Spring Boot

---

## 참조 Rule

- `docs/rules/springboot/security-rule.md`
- `docs/rules/springboot/api-response-rule.md`
- `docs/rules/springboot/dto-request-rule.md`
- `docs/rules/springboot/dto-response-rule.md`
- `docs/rules/springboot/exception-rule.md`
- `docs/rules/springboot/transaction-rule.md`

---

## Done Condition

- [ ] `POST /api/auth/signup` 회원가입이 동작한다
- [ ] `POST /api/auth/login` 성공 시 ACCESS_TOKEN, REFRESH_TOKEN, XSRF-TOKEN Cookie가 발급된다
- [ ] `GET /api/auth/me` 인증된 사용자 정보가 반환된다
- [ ] 인증되지 않은 요청에 401 응답이 반환된다
- [ ] `POST /api/auth/refresh` 토큰 갱신이 동작한다
- [ ] POST/PUT/DELETE 요청 시 CSRF 검증이 동작한다

---

## Out of Scope

- 소셜 로그인
- 이메일 인증
- 로그아웃 (2차 MVP) → 추후
- 회사 프로필 생성 → Epic-2

---

## 작업 항목

- [ ] JWT 의존성 추가 (jjwt)
- [ ] Spring Security 의존성 추가
- [ ] `JwtProvider` 구현 (토큰 생성 / 파싱 / 검증)
- [ ] `JwtAuthenticationFilter` 구현 (Cookie에서 Access Token 추출 및 인증)
- [ ] `SecurityFilterChain` 설정
  - 공개 경로: `/api/auth/signup`, `/api/auth/login`, `/api/auth/refresh`
  - XSRF-TOKEN Cookie 발급 설정
- [ ] `V6__create_refresh_tokens.sql` 작성 (refresh_tokens 테이블)
- [ ] `UserRepository` 구현 (email 단건 조회)
- [ ] `UserDetailsServiceImpl` 구현 (Spring Security `UserDetailsService` 인터페이스 구현, email로 User 로드)
- [ ] `AuthController` 구현 (signup, login, refresh, me)
- [ ] `AuthService` 구현
  - 회원가입: 비밀번호 BCrypt 암호화 저장
  - 로그인: 인증 후 Cookie 발급
  - 토큰 갱신: Refresh Token 검증 후 재발급
- [ ] Refresh Token DB 저장 구조 구현
- [ ] `SignupRequest`, `LoginRequest` DTO 작성 (Bean Validation 포함)
- [ ] `AuthUserResponse` DTO 작성
