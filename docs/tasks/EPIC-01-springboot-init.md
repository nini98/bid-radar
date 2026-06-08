# EPIC-01 Spring Boot 프로젝트 초기화

## 목표

Spring Boot 프로젝트를 초기화하고, 이후 Epic-1 작업의 기반이 되는 공통 구조(응답 Wrapper, 예외 처리, 설정 구조, DB 연결)를 구성한다.

---

## 파트

Spring Boot

---

## 참조 Rule

- `docs/rules/springboot/architecture-rule.md`
- `docs/rules/springboot/api-response-rule.md`
- `docs/rules/springboot/exception-rule.md`
- `docs/rules/springboot/config-property-rule.md`
- `docs/rules/springboot/flyway-db-migration-rule.md`

---

## Done Condition

- [ ] `./gradlew bootRun`으로 Spring Boot 앱이 정상 실행된다
- [ ] Docker Compose로 로컬 PostgreSQL이 실행되고 앱이 DB에 연결된다
- [ ] Flyway가 기동 시 실행되고 `flyway_schema_history` 테이블이 생성된다
- [ ] `GET /api/health` 요청 시 공통 Wrapper 형태로 200 응답이 반환된다
- [ ] `ApiException` 발생 시 `@RestControllerAdvice`에서 공통 Wrapper 형태 에러 응답이 반환된다

---

## Out of Scope

- 도메인 테이블 생성 (bid_notices, users 등) → `EPIC-01-db-schema`
- 나라장터 API 연동 → `EPIC-01-g2b-collector`
- JWT 인증 구조 → 별도 Task
- Frontend → `EPIC-01-bid-list-frontend`
- 운영 배포 환경 → `EPIC-01-docker-deploy`

---

## 작업 항목

- [ ] `backend/` 하위에 Spring Boot 프로젝트 생성
  - Gradle (Groovy DSL), Java 21, Spring Boot 3.x
  - 의존성: Spring Web, Spring Data JPA, PostgreSQL Driver, Flyway, Lombok, Validation, QueryDSL
- [ ] 패키지 구조 생성
  - `com.bidradar.bid`
  - `com.bidradar.auth`
  - `com.bidradar.common.response` (Response, ResultCode)
  - `com.bidradar.common.exception` (ApiException, GlobalExceptionHandler)
  - `com.bidradar.config`
- [ ] 공통 응답 구조 구현
  - `Response<T>` (header + data 구조)
  - `ResultCode` enum (SUCCESS, BAD_REQUEST, NOT_FOUND, INTERNAL_ERROR 등)
  - `ApiException(ResultCode)` 커스텀 예외
- [ ] `@RestControllerAdvice` 전역 예외 처리기 구현
  - `ApiException` 처리
  - `MethodArgumentNotValidException` 처리 (Bean Validation 실패)
  - fallback `Exception` 처리
- [ ] `application.yml` 작성
  - local 프로파일 기본 설정
  - PostgreSQL datasource 설정 (환경 변수로 주입)
  - `spring.jpa.hibernate.ddl-auto: validate`
  - Flyway 활성화
- [ ] `@ConfigurationProperties` 설정 클래스 기본 구조 작성
  - `AppProperties` (app.* prefix)
  - `G2bProperties` (app.g2b.* prefix, 나라장터 API 키 등)
- [ ] Docker Compose 작성 (`infra/docker-compose.local.yml`)
  - PostgreSQL 서비스
  - 환경 변수 설정
- [ ] Flyway 초기 마이그레이션 파일 추가
  - `V1__init.sql` (빈 파일 또는 extensions 설정)
- [ ] `GET /api/health` 헬스체크 엔드포인트 구현
  - 공통 Wrapper 적용 확인용
