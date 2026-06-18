# Testing Rule

## 1. 문서 목적

이 문서는 Java + Spring Boot + JPA + Gradle 환경에서 사용하는 테스트 코드 작성 규칙을 정의한다.
목표는 테스트 구조를 일관되게 유지하고, 회귀를 빠르게 발견할 수 있도록 검증 범위를 명확히 하는 것이다.

---

## 2. 기본 원칙

- 테스트는 구현 세부보다 관찰 가능한 동작을 검증한다.
- 가능한 한 작은 단위 테스트를 우선 작성한다.
- Spring Context 로딩은 꼭 필요한 경우에만 사용한다.
- 정상 흐름과 예외 흐름을 함께 검증한다.
- 테스트는 서로 독립적으로 실행 가능해야 한다.
- 랜덤 값, 현재 시각, 타임존 의존성은 제어 가능한 방식으로 다룬다.

---

## 3. 테스트 종류와 적용 기준

### 3-1. 단위 테스트

다음 대상은 단위 테스트를 우선한다.

- Service
- Factory
- Policy
- Utility
- Mapper (정책이 있는 경우)

규칙:

- 외부 의존성은 Mock으로 대체한다.
- 비즈니스 규칙, 예외 처리, 분기 로직을 중심으로 검증한다.
- Spring Context 없이 실행 가능하게 작성한다.

### 3-1-1. Service 테스트 규칙

- `@ExtendWith(MockitoExtension.class)` 기반으로 작성한다.
- 외부 의존성은 `@Mock`, 대상 Service는 `@InjectMocks`로 구성한다.
- Spring Context는 로드하지 않는다.
- 유스케이스 결과, 예외, 후속 처리 여부를 검증한다.
- Repository의 실제 쿼리, JPA 매핑, DB 제약 조건은 Service 테스트에서 검증하지 않는다.
- 하나의 테스트에서 여러 실패 분기를 동시에 검증하지 않는다.

### 3-1-2. Mapper 테스트 규칙

Mapper 테스트는 모든 mapper에 대해 기본적으로 작성하지 않는다.

직접 테스트가 필요한 경우:

- 입력 검증이 있는 경우
- 값 정규화가 있는 경우
- 계산 로직이 있는 경우
- null 처리나 기본값 보정이 있는 경우
- 특정 조건에서 예외가 발생하는 경우

우선순위가 낮은 경우:

- 단순 필드 복사만 수행하는 경우
- 상위 Service, Controller, Repository 테스트에서 충분히 간접 검증되는 경우

### 3-2. DB 통합 테스트

다음 대상은 DB 통합 테스트를 작성한다.

- JPA Repository
- QueryDSL 기반 조회
- Entity 매핑
- 트랜잭션 경계
- Flyway 마이그레이션 연동

규칙:

- `@DataJpaTest`를 기본으로 사용한다. JPA 슬라이스만 올라오므로 `@SpringBootTest`보다 빠르다.
- `@DataJpaTest`는 기본적으로 datasource를 H2로 교체한다. PostgreSQL 전용 기능(JSONB 등)을 사용하는 경우 `@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)`을 함께 선언해 실제 DB를 사용한다.
- Flyway 마이그레이션이 반영된 실제 스키마를 기준으로 검증한다.
- DB 조회/저장 결과를 실제로 검증해야 할 때 사용한다.
- Mock으로 충분한 로직에는 통합 테스트를 남용하지 않는다.

### 3-3. HTTP 클라이언트 통합 테스트

외부 API를 호출하는 클라이언트(RestClient, WebClient 등)는 WireMock으로 HTTP 계층을 제어해 테스트한다.

검증 대상:

- URL 조합 및 쿼리 파라미터 구성
- 응답 JSON 파싱 및 DTO 매핑
- 페이지네이션 동작
- API 오류 응답 처리 (서버가 에러 코드를 반환하는 경우)
- 응답 구조가 예상과 다른 경우 (필드 누락, 빈 배열 등)
- JSON 파싱 자체가 실패하는 경우 (깨진 응답)

방어 경로 테스트 원칙:

- 클라이언트 코드에 `catch`, null 체크, `isMissingNode()`, `isArray()` 같은 방어 로직이 있으면 해당 경로를 유발하는 테스트를 반드시 작성한다.
- 방어 로직이 "예외 없이 빈 결과 반환"이면, 테스트도 예외가 아닌 빈 결과를 단언한다.

규칙:

- 실제 외부 API를 호출하지 않는다. WireMock이 고정 응답을 반환한다.
- HTTP 클라이언트 테스트는 DB가 필요 없으므로 `@SpringBootTest` 없이 작성한다.

### 3-4. 애플리케이션 부팅 테스트

- `@SpringBootTest` 기반 `contextLoads` 테스트는 최소 수준으로 유지한다.
- 목적은 상세 기능 검증이 아니라 기본 부팅 가능 여부 확인이다.
- 단일 계층(DB 또는 HTTP 클라이언트)만 검증하는 용도로 `@SpringBootTest`를 쓰지 않는다. `@DataJpaTest` 또는 WireMock 기반 테스트로 대체한다.

### 3-5. 웹 슬라이스 테스트 (@WebMvcTest)

Controller 계층의 HTTP 규칙을 검증한다.
비즈니스 로직은 Service 단위 테스트에서 검증하며, Controller 테스트는 HTTP 계층에만 집중한다.

검증 대상:

- HTTP 메서드, URL 매핑 정확성
- 요청 파라미터/바디 바인딩 동작
- Bean Validation (`@Valid`, `@NotBlank` 등) 적용 여부
- 공통 응답 Wrapper 구조 (`header.resultCode`, `header.resultMessage`, `data`)
- 예외 발생 시 `GlobalExceptionHandler`를 통한 응답 형태
- HTTP 상태 코드 (200, 400, 404, 500 등)

검증하지 않는 것:

- 비즈니스 로직 결과 (Service 단위 테스트 담당)
- DB 쿼리 정확성 (DataJpaTest 담당)
- 실제 데이터 저장/조회 여부

#### 3-5-1. 기본 설정

- `@WebMvcTest(XxxController.class)` 로 대상 Controller만 지정한다. 전체 Controller를 올리지 않는다.
- Service 의존성은 `@MockitoBean`으로 대체한다.
- `MockMvc`를 `@Autowired`로 주입받아 사용한다.

```java
@WebMvcTest(BidNoticeController.class)
class BidNoticeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    BidNoticeService bidNoticeService;
}
```

#### 3-5-2. JSON 응답 검증

- `jsonPath()`로 응답 필드를 검증한다.
- 공통 응답 Wrapper 기준으로 `$.header.resultCode`, `$.data` 경로를 검증한다.

```java
mockMvc.perform(get("/api/bids/{id}", 1L))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.header.resultCode").value("0000"))
    .andExpect(jsonPath("$.data.title").value("테스트 공고"));
```

- 목록 응답은 `$.data.content[0].필드명` 경로로 검증한다.
- 에러 응답은 `$.header.resultCode`가 성공 코드가 아닌지, HTTP 상태 코드가 맞는지 검증한다.

#### 3-5-3. Validation 검증

- 필수 입력값 누락, 형식 오류 시 400 응답이 반환되는지 검증한다.
- Validation 실패 응답도 공통 Wrapper 형식을 유지하는지 확인한다.

```java
mockMvc.perform(post("/api/bids")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}"))  // 필수값 누락
    .andExpect(status().isBadRequest())
    .andExpect(jsonPath("$.header.resultCode").value("4000"));
```

#### 3-5-4. Spring Security 처리

- `@WebMvcTest`는 `SecurityFilterChain`을 함께 로드한다.
- 인증이 필요한 엔드포인트 테스트 시 `@WithMockUser`를 사용한다.
- 인증 구조 구현 전에는 테스트 전용 Security 설정 클래스를 만들어 `@Import`로 사용하거나, `@WebMvcTest`의 `excludeAutoConfiguration`으로 Security AutoConfiguration을 제외한다.

---

## 4. 테스트 도구 및 실행 명령

기본 실행 명령:

- `./gradlew test`
- `./gradlew build`

PR 또는 머지 전에는 최소 `./gradlew test`를 실행한다.

---

## 5. 테스트 코드 작성 규칙

- 테스트 파일은 `src/test/java` 아래, 운영 코드와 동일한 패키지 구조에 둔다.
- 파일명은 `*Test.java` 형식을 사용한다.
- 테스트 메서드명은 동작과 기대 결과가 드러나야 하며, `@DisplayName`으로 한글 설명을 붙이는 것을 허용한다.
- `given / when / then` 주석으로 테스트 구조를 구분한다. 생략하지 않는다.
- 하나의 테스트는 하나의 핵심 행위와 결과를 검증한다.
- 반환값, 상태 변화, 예외를 우선 검증한다.
- 내부 구현 호출 순서에 과도하게 결합된 검증은 지양한다.
- 엔티티에 상태 전이 메서드가 있으면 정상 흐름과 재호출/재처리 금지 같은 실패 흐름을 함께 검증한다.

---

## 6. Mock, Fixture, 시간 처리 규칙

- 외부 의존성은 `@Mock`, 대상 객체는 `@InjectMocks`로 구성한다.
- 사용하지 않는 stub은 만들지 않는다.
- 단순 DTO, 값 객체, 엔티티는 가능하면 실제 객체를 사용한다.
- 깊은 Mock 체인으로 테스트를 구성하지 않는다.
- 테스트 데이터는 각 테스트가 이해 가능한 수준으로 명시적으로 작성한다.
- 과도한 공용 fixture 유틸은 지양하고, 반복이 많을 때만 헬퍼를 도입한다.
- 현재 시각에 의존하는 코드는 `Clock` 주입을 우선한다.
- 랜덤 값과 시간은 고정 가능해야 하며, 타임존이 중요하면 `ZoneId`를 명시한다.

---

## 7. JPA 및 Spring 통합 테스트 규칙

- JPA 매핑, 실제 쿼리, QueryDSL 조회와 projection 결과는 `@DataJpaTest`로 검증한다. PostgreSQL 전용 기능을 쓰는 경우 `@AutoConfigureTestDatabase(replace = NONE)`을 함께 선언한다.
- Entity 연관관계, 저장/조회 결과, 제약 조건은 Mock 기반 단위 테스트로 대체하지 않는다.
- DB 스키마 검증은 Flyway 마이그레이션이 반영된 상태를 기준으로 한다.
- Spring 관련 테스트는 필요한 범위만 로드하고, 불필요한 전체 컨텍스트 로딩을 피한다.
- `@SpringBootTest`는 전체 컨텍스트가 필요한 경우에만 사용한다. DB만 필요하면 `@DataJpaTest`, HTTP 클라이언트만 필요하면 WireMock 기반 테스트가 적합하다.

---

## 8. 금지 사항

- 하나의 테스트에서 여러 분기와 여러 기대값을 동시에 검증하지 않는다.
- 통제할 수 없는 외부 API, 제3자 서비스에 의존하는 테스트를 작성하지 않는다. (로컬 Docker로 직접 띄우는 DB, 캐시 등 테스트 환경에서 통제 가능한 인프라는 허용한다.)
- 테스트 간 실행 순서에 의존하지 않는다.
- 현재 시각, 랜덤 값에 따라 결과가 바뀌는 테스트를 작성하지 않는다.
- 구현 세부사항만 검증하고 실제 결과를 검증하지 않는 테스트를 작성하지 않는다.
- Service 테스트에서 Repository/JPA의 실제 동작까지 함께 보장하려고 하지 않는다.

---

## 9. 핵심 규칙

- 작은 단위 테스트를 우선한다.
- Service, Factory, Policy는 Mock 기반 단위 테스트로 검증한다.
- Mapper는 정책, 검증, 계산이 있을 때만 선별적으로 테스트한다.
- Controller는 `@WebMvcTest`로 HTTP 계층만 검증하고, 비즈니스 로직은 Service 테스트에 맡긴다.
- JPA, QueryDSL, projection 매핑은 `@DataJpaTest`로 검증한다.
- 외부 HTTP API 클라이언트는 WireMock으로 검증한다.
- 시간과 랜덤 값은 제어 가능해야 한다.
