# Application Exception Rule

## 1. 문서 목적
- 애플리케이션 예외 처리 구조와 책임 분리를 공통 규칙으로 정의한다.
- 비즈니스 예외, 검증 예외, 시스템 예외를 일관되게 처리한다.
- 예외 응답 포맷은 `api-response-rule.md`를 따른다.

## 2. 예외 처리 기본 원칙
- Controller는 예외를 잡아 응답을 직접 조립하지 않는다.
- 비즈니스 실패는 `ResultCode` 기반 커스텀 예외로 표현한다.
- 검증 실패는 Bean Validation과 Spring 기본 예외로 처리한다.
- 인증/인가 실패는 일반 비즈니스 예외 처리 흐름과 분리하고 보안 계층에서 처리한다.
- 처리되지 않은 예외는 마지막에 `500`으로 수렴시킨다.

## 3. 예외 분류
### 비즈니스 예외
- 조회 대상 없음, 상태 불일치, 도메인 정책 위반 등 유스케이스 실패
### 검증 예외
- `@Valid`, `@NotBlank`, `@NotNull` 등 입력 검증 실패
- `MethodArgumentNotValidException`으로 수렴
### 시스템 예외
- 위 분류에 속하지 않는 런타임 오류, 외부 연동 오류, 버그

## 4. 커스텀 예외 사용 규칙
- 서비스 계층의 비즈니스 실패는 `ApiException(ResultCode)` 같은 커스텀 예외로 던진다.
- 커스텀 예외는 실패 의미만 담고 HTTP 응답 생성 책임은 가지지 않는다.
- 메시지는 기본적으로 `ResultCode`에서 가져온다.
- 조회 실패, 상태 검증 실패를 일반 `RuntimeException`으로 대체하지 않는다.

```java
public class ApiException extends RuntimeException {
    private final ResultCode resultCode;

    public ApiException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }
}
```

## 5. 레이어별 책임
### Controller
- 요청 바인딩과 서비스 호출만 담당한다.
- 예외를 catch 해서 `Response.fail(...)`을 만들지 않는다.
### Service
- 비즈니스 규칙을 검증하고 실패 시 커스텀 예외를 던진다.
- `orElseThrow(() -> new ApiException(...))` 패턴을 기본으로 사용한다.
- 예외를 삼키지 않고 상위로 전파한다.

## 6. 전역 예외 처리기 규칙
- REST API 예외 응답은 `@RestControllerAdvice`에서 일원화한다.
- 최소 처리 대상은 `ApiException`, `MethodArgumentNotValidException`, `Exception`이다.
- 각 핸들러는 `Response.fail(...)`로 응답을 만들고 HTTP status를 명시한다.
- `ApiException`은 `ResultCode`를 기준으로 HTTP status와 의미를 맞춘다.
- 검증 예외는 필드 오류를 문자열 또는 별도 오류 DTO로 축약해 반환한다.

## 7. fallback 예외 처리 규칙
- 마지막에는 반드시 `Exception` 핸들러를 둔다.
- 처리되지 않은 모든 예외는 `500 Internal Server Error`로 반환한다.
- 응답에는 일반화된 메시지를 사용하고 내부 상세는 로그로 남긴다.

## 8. 로그/민감정보 규칙
- 예외는 핸들러 또는 발생 지점에서 의미 있는 로그를 남긴다.
- 시스템 예외는 stack trace를 포함해 기록한다.
- 응답 본문에 stack trace, SQL, 내부 클래스명, 토큰, 개인정보를 노출하지 않는다.

## 9. 금지사항
- Controller에서 `try-catch`로 예외 응답을 직접 만드는 행위
- 서비스에서 `Response.fail(...)`을 직접 반환하는 행위
- 비즈니스 실패를 의미 없는 `RuntimeException` 문자열로 처리하는 행위
- 인증/인가 실패를 일반 비즈니스 예외 처리 규칙에 섞는 행위
- 운영 응답에 내부 예외 메시지와 민감 정보를 그대로 노출하는 행위

## 10. 체크리스트
- 비즈니스 실패가 `ResultCode` 기반 커스텀 예외로 표현되는가?
- `@RestControllerAdvice`가 애플리케이션 예외 응답을 일원화하는가?
- 검증, 비즈니스, 시스템 예외가 각각 올바른 핸들러로 수렴하는가?
- 응답 포맷이 `api-response-rule.md`와 일치하는가?
- 로그와 응답에서 민감 정보가 제거되는가?

## 11. 한 줄 요약
- 비즈니스 실패는 `ResultCode` 기반 커스텀 예외로 던진다.
- 애플리케이션 예외 응답은 `@RestControllerAdvice`에서 일원화한다.
- 인증/인가 실패는 보안 계층에서 별도로 처리한다.
- 처리되지 않은 예외는 마지막에 `500`으로 수렴시킨다.
