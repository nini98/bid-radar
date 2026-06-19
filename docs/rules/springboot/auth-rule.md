# Auth Rule

## 1. 문서 목적

이 프로젝트의 인증/인가 구현 방식을 정의한다.
원칙은 `security-rule.md`를 따르며, 이 문서는 이 프로젝트 고유의 구체적 결정 사항만 다룬다.

---

## 2. 인증 방식

- JWT + HttpOnly Cookie 방식을 사용한다.
- Access Token / Refresh Token을 분리한다.
- 토큰을 Response Body에 포함하지 않는다. Cookie로만 발급한다.

---

## 3. Cookie 구성

로그인 성공 시 3개의 Cookie를 발급한다.

| Cookie | HttpOnly | 용도 |
|---|---|---|
| `ACCESS_TOKEN` | ✓ | API 요청 인증 (XSS 방어) |
| `REFRESH_TOKEN` | ✓ | Access Token 재발급 (XSS 방어) |
| `XSRF-TOKEN` | ✗ | FE가 읽어 X-XSRF-TOKEN 헤더에 추가 (CSRF 방어) |

- `ACCESS_TOKEN`, `REFRESH_TOKEN`은 `HttpOnly`, `Secure`, `SameSite=Strict`로 설정한다.
- `Secure` 플래그는 운영 환경에서 반드시 활성화한다. 로컬 개발 환경에서는 `app.cookie.secure=false` 환경 변수로 토글을 허용한다.
- `XSRF-TOKEN`은 Spring Security 기본 Cookie 이름을 사용한다. FE가 JS로 읽을 수 있어야 하므로 HttpOnly를 적용하지 않는다.

---

## 4. 토큰 만료 시간 정책

| 토큰 | 만료 시간 |
|---|---|
| Access Token | 30분 |
| Refresh Token | 14일 |

---

## 5. Refresh Token DB 저장 방식

- Refresh Token은 발급 시 DB에 저장한다.
- 로그아웃 시 DB에서 삭제(무효화)한다.
- 토큰 갱신 시 DB의 Refresh Token과 대조한다. 일치하지 않으면 401을 반환한다.
- DB 저장 목적은 강제 로그아웃과 탈취된 토큰 무효화다.
- `users` 테이블에 `refresh_token`, `refresh_token_expires_at` 컬럼으로 관리한다.

---

## 6. CSRF 처리 방식

- GET 요청은 CSRF Token 불필요.
- POST / PUT / DELETE 요청 시 FE가 `X-XSRF-TOKEN` 헤더를 포함한다.
- `/api/auth/login`, `/api/auth/signup`은 로그인 전이므로 CSRF 적용 제외.
- `/api/auth/refresh`는 CSRF 적용 대상이다.

---

## 7. 토큰 갱신 흐름

```
Access Token 만료 → 401
→ POST /api/auth/refresh (REFRESH_TOKEN Cookie 자동 전송)
→ DB에서 Refresh Token 검증
→ 새 ACCESS_TOKEN + XSRF-TOKEN 발급
→ 요청 재시도
```

---

## 8. 공개 엔드포인트

인증 없이 접근 가능한 엔드포인트는 다음만 허용한다.

- `POST /api/auth/signup`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /api/health`

그 외 모든 엔드포인트는 인증 필요.

---

## 9. 인가 구조

- 권한은 `USER`, `ADMIN` 두 가지만 사용한다.
- `/api/admin/**` 경로는 `ADMIN` 권한만 접근 가능하다.
- 일반 사용자 기능은 `USER` 권한으로 충분하다.

---

## 10. 인증 실패 응답

인증/인가 실패도 공통 Wrapper를 유지한다.

```json
{ "header": { "resultCode": "401", "resultMessage": "인증이 필요합니다." }, "data": null }
{ "header": { "resultCode": "403", "resultMessage": "권한이 없습니다." }, "data": null }
```

Spring Security의 기본 응답 형식을 그대로 사용하지 않는다.
`AuthenticationEntryPoint`, `AccessDeniedHandler`를 커스텀 구현해 공통 Wrapper로 반환한다.

---

## 11. 금지 규칙

- 토큰을 Response Body나 LocalStorage에 저장하지 않는다.
- `permitAll`을 공개 엔드포인트 외에 편의상 넓게 열지 않는다.
- Refresh Token 없이 Access Token만으로 무한 연장하지 않는다.
- 인증 실패 응답에 토큰 검증 세부 내용을 노출하지 않는다.
