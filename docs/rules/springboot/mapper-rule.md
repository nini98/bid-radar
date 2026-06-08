# Mapper Rule

## 1. 문서 목적

이 문서는 Java + Spring Boot 프로젝트에서 DTO와 내부 모델 사이의 변환 규칙을 정의한다.
자동 매핑 도구가 아니라, 수동 변환 패턴을 기준으로 한다.

---

## 2. 핵심 패턴 요약

- 자동 매핑 도구를 전제로 하지 않고, 수동 매핑을 기본으로 사용한다.
- 입력 변환은 `Request/Query DTO → Criteria/내부 입력 모델` 같은 얇은 정규화에 집중한다.
- 엔티티 생성은 mapper보다 Factory와 service 조합으로 처리한다.
- 응답 변환은 엔티티 또는 읽기 모델을 `Response DTO`로 변환하는 전용 메서드에 둔다.
- 검색 목록처럼 repository가 projection DTO를 직접 반환하는 경우 별도 응답 mapper를 생략할 수 있다.
- service는 유스케이스 조합과 검증을 담당하고, mapper는 필드 이동과 형식 정규화만 담당한다.

---

## 3. 기본 원칙

### 3-1. 수동 매핑을 기본으로 한다

MapStruct 같은 자동 매핑 도구를 기본 전제로 두지 않는다.
변환 규칙은 코드에서 명시적으로 드러나야 한다.

허용 방식:

- `@Component` mapper 클래스 (static 메서드 또는 인스턴스 메서드)
- 도메인 클래스 내부 정적 팩토리 메서드 (`from(Entity entity)` 형태)

즉, 매핑은 숨기지 말고 읽는 사람이 바로 추적할 수 있게 둔다.

### 3-2. 변환 방향과 위치를 섞지 않는다

하나의 mapper 또는 메서드는 변환 방향을 명확히 가져야 한다.

- 입력 DTO → 내부 모델
- 엔티티/읽기 모델 → 응답 DTO

입력 변환과 응답 변환은 위치를 분리한다.
범용 거대 mapper 하나에 모든 변환을 몰아넣지 않는다.

---

## 4. 입력 DTO 변환 규칙

입력 DTO는 바로 repository나 domain에 넘기지 않고, 필요한 경우 내부 모델로 한 번 정규화한다.

입력 변환 mapper는 다음만 담당한다.

- 필드 이동
- nullable 값 정리
- 형식 수준 정규화
- 내부 조회 기준명으로의 변환
- 조회 경계값의 기술적 보정

비즈니스 의미가 있는 검증은 service 또는 별도 검증 계층이 담당한다.

### 4-1. Query DTO → Criteria

검색 조건은 query DTO를 그대로 쓰지 않고 criteria로 변환한다.

이 단계에서 허용되는 작업:
- 기간 값 형식 정리
- inclusive/exclusive 같은 조회 경계 보정
- API 입력명을 내부 조회 기준명으로 치환

이 단계에서 하지 않는 작업:
- repository 호출
- 엔티티 조회
- 비즈니스 규칙 검증
- 상태 판단

---

## 5. 엔티티 생성과 mapper의 경계

Request DTO를 곧바로 엔티티로 복사하는 방식을 기본 규칙으로 삼지 않는다.

다음 요소가 개입하면 엔티티 생성은 mapper보다 Factory 또는 service가 담당한다.

- 연관 엔티티 참조 필요
- 기본 상태값 설정 필요
- 서버 생성 값 필요
- 도메인 규칙 적용 필요
- 여러 입력을 조합해야 함

즉, 엔티티 생성은 단순 필드 복사가 아니라 도메인 생성 행위이므로, mapper보다 Factory에 두는 것을 우선한다.

---

## 6. 응답 DTO 변환 규칙

응답 변환은 엔티티 또는 읽기 모델을 `Response DTO`로 바꾸는 전용 메서드에 둔다.
단, repository가 읽기 전용 projection DTO를 직접 반환하는 경우에는 별도 응답 변환 단계를 두지 않을 수 있다.

권장 방식:

- Response DTO 내부 정적 팩토리 메서드: `BidSummaryResponse.from(bid)`
- Mapper 클래스의 변환 메서드: `bidMapper.toSummary(bid)`

즉, 응답 변환은 큰 mapper 한 개보다 작은 변환 메서드들의 조합으로 유지한다.

### 6-1. null 안전성 규칙

응답 변환 시 필수 값은 명시적으로 보장한다.
필요하면 `Objects.requireNonNull()` 같은 방식으로 누락을 즉시 드러낸다.

### 6-2. 계산 필드 규칙

응답 DTO에 단순 파생값이 필요하면 변환 메서드 안에서 계산할 수 있다.
단, 도메인 규칙 수준의 계산이나 집계 로직은 mapper에 두지 않는다.

---

## 7. Projection DTO 규칙

검색 목록이나 읽기 전용 조회는 repository가 projection DTO를 직접 반환할 수 있다.
이 경우 service는 결과를 감싸거나 반환 형식만 조합하고, 별도 응답 mapper를 거치지 않을 수 있다.

단, 다음을 기본 원칙으로 한다.

- repository projection DTO와 API response DTO는 기본적으로 동일 클래스로 재사용하지 않는다.
- projection DTO는 조회 최적화를 위한 내부 읽기 모델로 본다.
- API 응답 스펙은 가능하면 별도 response DTO로 유지한다.

---

## 8. Service와 Mapper 책임 경계

### 8-1. Mapper 책임
- 필드 매핑
- 입력 정규화
- 조회 경계값 보정
- 응답 DTO 조립
- 작은 파생값 계산

### 8-2. Service 또는 검증 계층 책임
- repository 호출
- 연관 엔티티 조회
- 비즈니스 유효성 검증
- 유스케이스 조합
- Factory 호출

---

## 9. 금지 규칙

- Request DTO를 그대로 엔티티에 복사하는 방식을 기본 규칙으로 삼지 않는다.
- mapper 안에서 repository를 호출하지 않는다.
- mapper 안에서 비즈니스 규칙 검증이나 상태 전이를 수행하지 않는다.
- projection DTO를 API response DTO나 쓰기 유스케이스 입력 모델로 재사용하지 않는다.
- 범용 거대 mapper 하나에 모든 변환을 몰아넣지 않는다.

---

## 10. 한 줄 요약

- 입력 DTO는 필요하면 criteria나 내부 입력 모델로 수동 변환한다.
- 엔티티 생성은 mapper보다 Factory와 service 조합을 우선한다.
- 응답 변환은 정적 팩토리 메서드 또는 작은 변환 메서드로 분리한다.
- projection DTO와 API response DTO는 기본적으로 분리한다.
- mapper는 변환과 정규화만, service는 조회와 검증과 조합만 담당한다.
