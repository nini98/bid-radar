# Spring Boot Review Rule

## 1. 문서 목적

Spring Boot 코드 완료 후 공통 검토 기준을 정의한다.
아키텍처 경계, 계층 책임, 공통 규칙 위반 여부를 확인한다.

---

## 2. 아키텍처 경계 검토

- Controller가 Repository를 직접 호출하지 않는가?
- Controller가 엔티티를 요청/응답 모델로 직접 사용하지 않는가?
- Service가 HTTP 객체, 웹 프레임워크 타입에 의존하지 않는가?
- Service가 SQL, JPQL, QueryDSL을 직접 작성하지 않는가?
- Repository가 비즈니스 정책, 계산 규칙, 유스케이스 조합을 갖지 않는가?
- `common`에 특정 도메인 정책이 들어가 있지 않는가?

---

## 3. API 응답 검토

- 모든 API 응답이 `Response<T>` 공통 래퍼를 사용하는가?
- 성공/실패 모두 `header + data` 구조를 유지하는가?
- 비즈니스 실패가 `ResultCode`로 표준화되어 있는가?
- HTTP Status와 `ResultCode.code`가 동일한 HTTP 의미 체계를 따르는가?
- 예외 응답이 `@RestControllerAdvice`에서 일원화되는가?
- 실패 `data`에 stack trace, SQL, 민감 정보가 포함되지 않는가?

---

## 4. 엔티티 검토

- JPA 무인자 생성자가 `@NoArgsConstructor(access = AccessLevel.PROTECTED)`로 제한되어 있는가?
- 엔티티에 `@Data` 또는 무분별한 public setter가 없는가?
- enum이 `@Enumerated(EnumType.STRING)`으로 저장되는가?
- 상태 변경이 목적 있는 메서드로 표현되는가?
- 상태 전이 메서드가 재처리 금지 규칙을 검증하는가?
- 엔티티가 Controller 응답으로 직접 반환되지 않는가?
- `@ManyToOne`에 `fetch = FetchType.LAZY`가 설정되어 있는가?
- `@JoinColumn`에 컬럼명, nullable, FK 이름이 명시되어 있는가?

---

## 5. Repository 검토

- 동적 조회, projection, 벌크 수정이 Custom Repository로 분리되어 있는가?
- QueryDSL이 `RepositoryImpl` 내부에서만 사용되는가?
- 외부 API의 Query DTO가 Repository에 직접 전달되지 않는가?
- 단건 조회 반환 타입이 `Optional`로 일관되는가?
- 다건 조회가 `null` 대신 빈 컬렉션을 반환하는가?

---

## 6. 트랜잭션 검토

- 쓰기 유스케이스에 `@Transactional`이 적용되어 있는가?
- 조회 전용 메서드에 `@Transactional(readOnly = true)`가 명시되어 있는가?
- 읽기 트랜잭션 안에서 저장이나 상태 변경이 없는가?
- 트랜잭션 내부에서 예외를 삼키지 않는가?
- 외부 API 호출이 긴 트랜잭션 안에 묶여 있지 않는가?
- `@Transactional`/`@Async` 메서드를 같은 클래스 내부에서 직접 호출(self-invocation)하지 않는가? (프록시 우회 시 호출자와 같은 스레드에서 동기 실행됨 — 로그의 스레드 이름으로 확인)
- 같은 트랜잭션 안에서 실행 순서가 결과에 영향을 주는 영속성 작업(제약조건이 걸린 삭제 후 재삽입, 앞선 변경을 전제로 한 native query/조회 등)에 명시적 `flush()`가 있는가?

---

## 7. DTO 검토

- Request DTO가 엔티티를 직접 포함하거나 상속하지 않는가?
- 생성/수정/검색 DTO가 분리되어 있는가?
- 단순 형식 검증이 Request DTO에서 Bean Validation으로 처리되는가?
- 목록 API가 Summary DTO를 사용하고 상세 API가 Detail DTO를 사용하는가?
- Response DTO가 엔티티 전체를 그대로 노출하지 않는가?

---

## 8. 예외 처리 검토

- 비즈니스 실패가 `ApiException(ResultCode)` 형태의 커스텀 예외로 표현되는가?
- Controller에서 예외를 직접 catch해 응답을 만들지 않는가?
- 인증/인가 실패가 일반 비즈니스 예외 처리 흐름과 분리되어 있는가?
- 전역 예외 처리기에 fallback `Exception` 핸들러가 있는가?

---

## 9. 보안 검토

- 민감정보(토큰, API 키, 비밀번호)가 코드에 하드코딩되지 않는가?
- 민감정보가 로그에 출력되지 않는가?
- `permitAll`이 꼭 필요한 공개 엔드포인트에만 적용되었는가?

---

## 10. 설정 검토

- 민감정보가 환경 변수로 외부 주입되는가?
- 정책성 숫자/시간 값이 코드에 매직 넘버로 하드코딩되지 않았는가?
- 여러 설정이 `@ConfigurationProperties`로 묶여 있는가?

---

## 11. Flyway 검토 (스키마 변경 시)

- 마이그레이션 파일명이 `V{n}__description.sql` 형식인가?
- `spring.jpa.hibernate.ddl-auto=validate` 설정이 유지되는가?
- 컬럼 추가 시 기본값, 데이터 보정, 인덱스 필요 여부를 함께 검토했는가?
- 제약조건과 인덱스에 이름이 명시되어 있는가?
- 이미 적용된 마이그레이션 파일을 수정하지 않았는가?

---

## 12. 로그 검토

- 민감정보가 로그에 출력되지 않는가?
- 같은 예외를 여러 계층에서 중복 로그하지 않는가?
- 정상 흐름을 `INFO`로 과도하게 기록하지 않는가?

---

## 13. 테스트 검토

### 공통 게이트 (항상 필수)
- `./gradlew test`가 통과하는가?

### 단위 테스트 작성 기준
아래 중 하나라도 해당하면 단위 테스트를 작성한다.
- Service 메서드에서 입력값/상태에 따라 다른 비즈니스 결과가 나오는 경우
  (예: 공고 상태가 A면 예외, B면 저장, C면 스킵)
- 점수, 금액, 날짜를 계산하는 로직
- 엔티티 상태 전이 메서드 (activate, cancel, complete 등)

아래에 해당하면 단위 테스트를 생략할 수 있다.
- 조회 후 저장만 하는 단순 Service (분기 없음)
- 단순 필드 복사만 하는 Mapper
- ConfigurationProperties 등 설정 클래스

### 통합 테스트 작성 기준
아래 중 하나라도 해당하면 통합 테스트를 작성한다.
- `BooleanBuilder`로 조건을 동적으로 조합하는 QueryDSL 쿼리
  (예: keyword, region, budget 등 선택적 필터가 있는 목록 조회)
- 파라미터에 따라 정렬 기준이 바뀌는 쿼리
- COUNT, SUM 등 집계 쿼리

아래에 해당하면 통합 테스트를 생략할 수 있다.
- 고정 조건의 단순 조회 (`findByStatus`, `findById` 등)
- JpaRepository 기본 메서드만 사용하는 CRUD
