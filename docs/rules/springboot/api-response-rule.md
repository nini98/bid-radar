# API 공통 Response 규칙

## 1. 문서 목적

- REST API 응답 포맷에 대한 프로젝트 공통 규칙을 정의한다.
- 성공/실패 응답 구조를 일관되게 유지한다.
- 다른 프로젝트에서도 재사용 가능한 최소 공통 구조를 기준으로 삼는다.

---

## 2. 기준 구조

공통 Response 코드는 아래 3개 타입을 중심으로 구성된다.

- `Header`
- `Response<T>`
- `ResultCode`

기본 응답 형태는 아래와 같다.

```json
{
  "header": {
    "resultCode": "200",
    "resultMessage": "성공"
  },
  "data": {}
}
```

실패 응답도 동일한 구조를 유지한다.

```json
{
  "header": {
    "resultCode": "400",
    "resultMessage": "입력한 값이 유효하지 않습니다. 필수 항목을 확인해 주세요."
  },
  "data": "field: must not be blank"
}
```

---

## 3. 기본 설계 원칙

- 모든 REST API 응답은 공통 래퍼 `Response<T>`를 사용한다.
- 응답의 상태 정보는 HTTP Status와 동일한 의미 체계를 `header`에 한 번 더 명시한다.
- 성공/실패 모두 동일한 JSON 골격을 유지한다.
- 비즈니스 실패는 문자열 하드코딩이 아니라 `ResultCode` enum으로 관리한다.
- 예외 응답은 각 컨트롤러에서 직접 조립하지 않고 전역 예외 처리기에서 일괄 생성한다.

---

## 4. 표준 타입 규칙

### 4-1. Header

- `Header`는 반드시 아래 두 필드를 가진다.
  - `resultCode: String`
  - `resultMessage: String`
- `resultCode`는 외부 클라이언트가 안정적으로 해석할 수 있는 고정 코드여야 한다.
- `resultMessage`는 사용자 또는 프론트엔드가 바로 활용할 수 있는 메시지여야 한다.
- 성공 헤더 생성은 팩토리 메서드로 통일한다.

```java
@Getter
@AllArgsConstructor
public class Header {
    private final String resultCode;
    private final String resultMessage;

    public static Header success() {
        return new Header("200", "성공");
    }
}
```

### 4-2. Response<T>

- 모든 API 응답은 `Response<T>`를 반환한다.
- `data`는 nullable 허용을 기본으로 한다.
- 데이터가 없는 성공 응답도 빈 JSON 대신 `Response.success()` 형태로 반환한다.

```java
@Getter
@AllArgsConstructor
public class Response<T> {
    private final Header header;
    private final T data;

    public static <T> Response<T> success(T data) {
        return new Response<>(Header.success(), data);
    }

    public static Response<Void> success() {
        return new Response<>(Header.success(), null);
    }

    public static <T> Response<T> fail(ResultCode resultCode, T data) {
        return new Response<>(new Header(resultCode.getCode(), resultCode.getMessage()), data);
    }
}
```

### 4-3. ResultCode

- `ResultCode`는 공통 실패 정책의 단일 진입점이다.
- 각 항목은 최소 아래 정보를 가진다.
  - `code: String`
  - `message: String`
- 코드 추가 기준은 "여러 API에서 반복적으로 재사용되는 실패 의미인가"로 판단한다.

```java
@Getter
@RequiredArgsConstructor
public enum ResultCode {
    SUCCESS("200", "성공"),
    VALIDATION_ERROR("400", "입력한 값이 유효하지 않습니다."),
    UNAUTHORIZED("401", "인증이 필요합니다."),
    FORBIDDEN("403", "권한이 없습니다."),
    NOT_FOUND("404", "리소스를 찾을 수 없습니다."),
    CONFLICT("409", "이미 존재합니다."),
    INTERNAL_ERROR("500", "서버 오류가 발생했습니다.");

    private final String code;
    private final String message;
}
```

---

## 5. 성공 응답 규칙

- 성공 응답은 반드시 `Response.success(...)` 팩토리 메서드를 사용한다.
- 컨트롤러에서 `Header`를 직접 생성하지 않는다.
- 생성, 수정, 삭제처럼 반환 본문이 불필요한 경우 `Response.success()`를 사용한다.

---

## 6. 실패 응답 규칙

- 실패 응답은 반드시 `Response.fail(resultCode, data)` 팩토리 메서드를 사용한다.
- 실패 원인의 대표 의미는 `header`에 담고, 상세 원인이나 부가 설명은 `data`에 담는다.
- `ResultCode.code`는 표준 HTTP Status와 의미상 동일한 값으로 사용한다.
- 프론트엔드 분기 목적만으로 비표준 HTTP 숫자 코드를 새로 만들지 않는다.

---

## 7. HTTP Status와 ResultCode 매핑 규칙

권장:
- 성공 → `200`
- 잘못된 입력 → `400`
- 인증 실패 → `401`
- 권한 없음 → `403`
- 데이터 없음 → `404`
- 중복/충돌 → `409`
- 서버 오류 → `500`

금지:
- 세션 만료 → `419`
- 토큰 만료 → `498`
- 토큰 오류 → `499`

---

## 8. 예외 처리 규칙

- 컨트롤러와 서비스는 공통 실패 응답을 직접 조립하지 않는다.
- 비즈니스 예외는 `ResultCode`를 포함한 커스텀 예외로 던진다.
- 전역 예외 처리기(`@RestControllerAdvice`)가 예외를 `Response.fail(...)`로 변환한다.

---

## 9. data 필드 사용 규칙

- `data`는 성공 시 응답 DTO, 리스트, 페이지 응답 등 실제 본문을 담는다.
- 실패 `data`에 스택 트레이스, 내부 클래스명, SQL, 민감 정보는 포함하지 않는다.

---

## 10. Controller 반환 규칙

- Controller의 반환 타입은 `Response<T>` 또는 `ResponseEntity<Response<T>>`로 통일한다.
- 정상 흐름에서는 `Response<T>` 반환을 기본으로 한다.
- HTTP Status를 명시적으로 제어해야 하는 경우 `ResponseEntity<Response<T>>`를 사용한다.
- 컨트롤러는 도메인 엔티티를 직접 반환하지 않고 항상 Response DTO를 `data`에 담는다.

---

## 11. 패키지 위치 규칙

- 공통 REST 응답 래퍼는 `common.response` 패키지에 둔다.
- 전역 예외 처리기는 `common.exception` 패키지에 둔다.

---

## 12. 판단 기준 체크리스트

- 이 API 응답이 공통 래퍼 `Response<T>`를 따르는가?
- 성공/실패 모두 `header + data` 구조를 유지하는가?
- 실패 사유가 `ResultCode`로 표준화되어 있는가?
- 검증/인증/인가/비즈니스 예외가 전역 예외 처리기로 수렴되는가?
- 실패 `data`에 민감 정보가 포함되지 않는가?

---

## 13. 한 줄 규칙 요약

- 모든 REST 응답은 `Response<T>`
- 상태 정보는 `header`
- 실제 본문은 `data`
- 실패는 `ResultCode`
- 예외 응답 생성은 전역 예외 처리기에서 일원화
