# EPIC-01 인증 구조 구현 (JWT Cookie)

## 목표

JWT Cookie 기반 인증 구조를 구현한다. 회원가입, 로그인, 토큰 갱신, 내 정보 조회 API를 제공한다.

---

## 파트

Spring Boot

---

## 참조 Rule

- `docs/rules/springboot/architecture-rule.md`
- `docs/rules/springboot/auth-rule.md`
- `docs/rules/springboot/security-rule.md`
- `docs/rules/springboot/entity-rule.md`
- `docs/rules/springboot/flyway-db-migration-rule.md`
- `docs/rules/springboot/api-response-rule.md`
- `docs/rules/springboot/dto-request-rule.md`
- `docs/rules/springboot/dto-response-rule.md`
- `docs/rules/springboot/exception-rule.md`
- `docs/rules/springboot/transaction-rule.md`
- `docs/rules/springboot/logging-rule.md`

---

## Done Condition

- [ ] `POST /api/auth/signup` 회원가입이 동작한다
- [ ] `POST /api/auth/login` 성공 시 ACCESS_TOKEN, REFRESH_TOKEN, XSRF-TOKEN Cookie가 발급된다
- [ ] `GET /api/auth/me` 인증된 사용자 정보가 반환된다
- [ ] 인증되지 않은 요청에 공통 Wrapper 형태로 401 응답이 반환된다
- [ ] `POST /api/auth/refresh` 토큰 갱신이 동작한다
- [ ] POST/PUT/DELETE 요청 시 CSRF 검증이 동작한다

---

## Out of Scope

- 소셜 로그인
- 이메일 인증, 비밀번호 찾기
- 회원 탈퇴, 비밀번호 변경
- 회사 프로필 생성 → Epic-2

---

## 작업 항목

### DB / 엔티티
- [ ] `V6__alter_users_add_refresh_token.sql` 작성
  - `users` 테이블에 `refresh_token VARCHAR(512)`, `refresh_token_expires_at TIMESTAMP` 컬럼 추가
- [ ] `User` 엔티티에 `refreshToken`, `refreshTokenExpiresAt` 필드 추가
  - `updateRefreshToken(token, expiresAt)` 상태 변경 메서드 추가
  - `revokeRefreshToken()` 로그아웃 시 무효화 메서드 추가

### 의존성
- [ ] `build.gradle`에 Spring Security, JWT(jjwt) 의존성 추가

### 인증 인프라
- [ ] `JwtProvider` 구현
  - Access Token 생성 (만료 30분), Refresh Token 생성 (만료 14일)
  - 토큰 파싱 및 유효성 검증
  - 토큰에서 userId, role 추출
- [ ] `JwtAuthenticationFilter` 구현
  - 요청 Cookie에서 ACCESS_TOKEN 추출
  - 유효하면 `SecurityContextHolder`에 인증 정보 설정
- [ ] `SecurityConfig` 구현
  - `JwtAuthenticationFilter` 등록
  - 공개 엔드포인트 설정 (`/api/auth/signup`, `/api/auth/login`, `/api/auth/refresh`, `/api/health`)
  - CSRF 설정 (`CookieCsrfTokenRepository` 사용)
  - `AuthenticationEntryPoint` 커스텀 구현 (401 → 공통 Wrapper)
  - `AccessDeniedHandler` 커스텀 구현 (403 → 공통 Wrapper)
- [ ] `CookieProvider` 구현
  - ACCESS_TOKEN, REFRESH_TOKEN HttpOnly Cookie 생성/삭제
  - XSRF-TOKEN Cookie 생성/삭제

### Repository
- [ ] `UserRepository` 구현
  - `findByEmail(String email)`
  - `findByRefreshToken(String refreshToken)`

### Service
- [ ] `UserDetailsServiceImpl` 구현 (Spring Security 연동)
- [ ] `AuthService` 구현
  - `signup`: 이메일 중복 확인, 비밀번호 BCrypt 해시
  - `login`: 이메일/비밀번호 검증, 토큰 생성, Refresh Token DB 저장, 마지막 로그인 갱신
  - `refresh`: DB에서 Refresh Token 검증, 만료 확인, 새 Access Token 발급
  - `logout`: Refresh Token DB 무효화, Cookie 삭제

### Controller / DTO
- [ ] `AuthController` 구현
  - `POST /api/auth/signup`
  - `POST /api/auth/login`
  - `POST /api/auth/refresh`
  - `GET /api/auth/me`
  - `POST /api/auth/logout`
- [ ] `SignupRequest` DTO 작성 (email, password, name — Bean Validation 포함)
- [ ] `LoginRequest` DTO 작성 (email, password — Bean Validation 포함)
- [ ] `AuthUserResponse` DTO 작성 (id, email, name, role)
