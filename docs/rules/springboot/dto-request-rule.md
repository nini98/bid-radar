# Request DTO Rule

## 1. 문서 목적

이 문서는 Request DTO의 네이밍, 역할, 위치에 대한 공통 규칙을 정의한다.
목표는 API 입력 구조를 일관되게 유지하고, 검증과 유지보수를 쉽게 하는 것이다.

---

## 2. 기본 설계 원칙

- Controller는 클라이언트 입력을 항상 DTO로 받는다.
- 엔티티를 Request DTO로 직접 받지 않는다.
- 생성, 수정, 검색 DTO는 하나로 혼합하지 않는다.
- Request DTO는 입력 의도만 표현하고 계산값, 결과값, 상태 판단을 포함하지 않는다.
- Response DTO와 재사용하지 않는다.

---

## 3. Request DTO 분류 기준

Request DTO는 전송 방식 기준으로 두 가지로 구분한다.

### 1) Body Request DTO

- `@RequestBody` 기반 JSON 입력
- POST, PUT, PATCH 계열 API에서 사용
- 생성, 수정, 상태 변경 같은 command 입력

### 2) Query Condition DTO

- GET 요청의 Query Parameter 입력
- `@ModelAttribute` 바인딩 사용
- 목록, 검색, 필터링 조건 표현 목적

---

## 4. Body Request DTO 네이밍 규칙

### 4-1. 기본 CRUD

- POST: `{Domain}CreateRequest`
- PUT: `{Domain}UpdateRequest`
- PATCH: `{Domain}PatchRequest`
- DELETE: 기본적으로 DTO 없이 Path Variable 사용

### 4-2. 유스케이스 전용 Request

상태 변경이나 의미 있는 동작은 동사 기반 Request DTO로 분리한다.

예:
- `CancelBidRequest`
- `RecalculateScoreRequest`

---

## 5. Body Request DTO 설계 규칙

### 5-1. 입력 의도 중심 설계

- 계산 가능한 값은 Request로 받지 않는다.
- 서버가 결정해야 하는 값은 Request로 받지 않는다.
- 연관 엔티티는 객체가 아니라 ID로만 전달한다.

### 5-2. Validation 규칙

- 단순 형식, 필수값, 범위 검증은 Request DTO에서 처리한다.
- Bean Validation(`@Valid`, `@NotBlank`, `@NotNull` 등) 사용을 기본으로 한다.
- 비즈니스 규칙 검증과 상태 전이 가능 여부 검증은 Service에서 처리한다.

---

## 6. 중첩 DTO 사용 원칙

- Request DTO 내부에 Request DTO 중첩을 허용한다.
- 리스트 입력이 필요하면 하위 요소는 별도 DTO로 분리한다.
- Patch Request는 최소 변경 단위만 포함한다.

---

## 7. Query Condition DTO 네이밍 규칙

- 목록, 검색용 Query DTO는 `{Domain}SearchCondition`을 표준으로 사용한다.
- Query Condition DTO는 Body Request DTO와 분리한다.
- 파라미터가 1~2개 수준이면 DTO 없이 `@RequestParam` 사용을 허용한다.

---

## 8. Query Condition DTO 설계 규칙

- 조회 필터링 목적에만 사용하고 상태 변경 목적으로 사용하지 않는다.
- 모든 필드는 nullable을 기본으로 한다.
- 값이 없는 조건은 무시한다.
- 기간 조건 필드는 `{field}From`, `{field}To` 네이밍을 권장한다.
- 페이징은 `Pageable`을 우선 사용하고 Query DTO와 섞지 않는다.
- 단, `Pageable.Sort`를 사용하지 않는 경우(정렬 로직이 DB 컬럼명으로 표현되지 않거나 별도 파라미터로 정렬을 제어하는 경우)에는 `page` / `size`를 Query DTO에 포함하는 방식을 허용한다.

---

## 9. DTO 위치 규칙

- `@RequestBody` 입력 DTO는 해당 도메인의 `dto/request`에 둔다.
- GET 검색 조건 DTO는 `dto/query`에 둔다.
- Response DTO는 `dto/response`에 둔다.
- 서비스 내부 계산이나 조회 조합용 객체는 API 입력 DTO와 분리하고 `service` 내부 패키지에 둔다.

즉, 외부 입력과 서비스 내부 해석 객체를 분리한다.

---

## 10. 핵심 요약

- 입력은 항상 Request DTO로 받는다.
- POST/PUT/PATCH는 목적별 DTO로 분리한다.
- 상태 변경은 유스케이스 전용 Request로 분리한다.
- 검색은 `SearchCondition` 계열 DTO로 분리한다.
- 연관 관계는 ID로만 전달한다.
- 단순 형식 검증은 DTO에서, 비즈니스 검증은 Service에서 처리한다.
- 외부 입력 DTO와 내부 criteria 객체를 혼용하지 않는다.
