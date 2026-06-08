# Repository Rule

## 1. 문서 목적

이 문서는 Repository 설계 규칙을 정의한다.
특정 프로젝트 구현을 복제하는 것이 아니라 검증된 패턴을 일반화한 기준이다.

---

## 2. Repository 분리 원칙

Repository는 다음 두 형태로 구분한다.

### 1) 기본 Repository

다음 경우에는 `JpaRepository`만 선언한다.

- 단순 저장
- ID 기반 조회
- 파생 메서드 조회
- 고정형 단건 조회

### 2) Custom Repository

다음 경우에는 `RepositoryCustom`과 `RepositoryImpl`을 분리한다.

- 동적 조건 조회
- DTO projection 조회
- 배치용 읽기 모델 조회
- 벌크 상태 변경
- 기본 CRUD로 표현하기 어려운 쿼리

즉, 단순 영속성 접근은 기본 Repository에 두고, 쿼리 성격이 강한 로직은 Custom Repository로 분리한다.

---

## 3. Spring Data JPA / Custom Repository / QueryDSL / JPQL 역할 규칙

### 3-1. Spring Data JPA

기본 Repository에는 다음만 둔다.

- `save`, `saveAll`, `findById`, `findAllById` 같은 기본 메서드
- 파생 메서드
- 단순하고 고정된 형태의 조회
- 필요 시 `RepositoryCustom` 상속

### 3-2. Custom Repository

Custom Repository에는 다음을 둔다.

- 동적 조회
- DTO 또는 row projection 조회
- 벌크 update
- 배치/집계용 source 조회

Custom Repository는 영속성 접근만 담당한다.
비즈니스 계산이나 유스케이스 조립을 넣지 않는다.

### 3-3. QueryDSL

QueryDSL은 `RepositoryImpl` 내부에서만 사용한다.

다음 경우 QueryDSL을 사용한다.

- 조건이 동적으로 조합되는 조회
- DTO projection 반환
- ID 목록 조회
- 벌크 상태 변경

권장 패턴:

- `JPAQueryFactory`는 공통 config에서 Bean으로 생성한다.
- `RepositoryImpl`이 이를 주입받아 사용한다.
- 동적 조건은 `BooleanBuilder` 또는 동등한 방식으로 조합한다.
- 페이지 조회는 content와 total을 분리해서 조회한다.

### 3-4. JPQL

JPQL은 기본 Repository 인터페이스에 둔 고정형 엔티티 조회에만 제한적으로 사용한다.

허용 대상:

- 단순 fetch join 조회
- 조건 조합이 없는 고정형 엔티티 조회

동적 조건 조회, DTO projection 조회, 벌크 수정은 JPQL보다 Custom Repository + QueryDSL을 우선한다.

### 3-5. fetch join과 pagination 주의사항

컬렉션 fetch join과 pagination을 기본 조합으로 사용하지 않는다.

페이지 조회가 필요하면 엔티티 컬렉션을 fetch join으로 직접 조회하는 방식보다 다음 방식을 우선 검토한다.

- ID 목록을 먼저 조회한 뒤 필요한 엔티티를 재조회하는 방식
- DTO projection 기반 페이지 조회 방식

---

## 4. 엔티티 조회와 DTO 조회 분리 규칙

조회 목적에 따라 반환 타입을 분리한다.

### 1) 엔티티 조회를 우선하는 경우

- 상세 조회
- 연관 엔티티 탐색이 필요한 경우
- 조회 결과를 Service에서 도메인 로직과 함께 해석해야 하는 경우

### 2) DTO 또는 row projection을 우선하는 경우

- 목록 조회
- 검색 조회
- 통계/집계 입력 조회
- 배치 처리용 source 조회

목록/검색용 데이터를 위해 엔티티를 조회한 뒤 Service에서 다시 요약 DTO로 변환하는 구조를 기본 방식으로 사용하지 않는다.

---

## 5. Service와 Repository 책임 경계

### 5-1. Repository 책임

허용: 저장, 단순 조회, 동적 조회, DTO projection 조회, 배치 대상 조회, 벌크 상태 변경

금지: 유스케이스 조립, 비즈니스 계산, 그룹핑, 예외 정책 결정, 요청 DTO 해석

### 5-2. Service 책임

Service는 다음을 담당한다.

- 트랜잭션 경계 설정
- 요청 DTO를 내부 criteria로 변환
- 여러 Repository 호출 순서 조합
- 계산, 그룹핑, 집계 해석
- 엔티티/row를 응답 DTO나 도메인 결과로 조합
- 예외 처리 정책 적용

---

## 6. 추가 규칙

### 6-1. 검색 조건 전달 규칙

외부 API의 Query DTO를 Repository에 직접 넘기지 않는다.
Repository에는 내부 조회 기준 객체를 전달한다.

### 6-2. 벌크 상태 변경 규칙

다건 상태 변경은 엔티티 반복 수정이 아니라 Repository의 벌크 update로 처리한다.
가능하면 현재 상태를 where 조건에 포함해 잘못된 상태 전이를 줄인다.

### 6-3. 참조 엔티티 연결 규칙

연관 엔티티 전체 조회가 필요 없고 FK 연결만 필요하면 `EntityManager.getReference()` 사용을 허용한다.
단, 참조 대상의 존재와 의미가 상위 로직에서 이미 확정된 경우에만 사용한다.

### 6-4. Custom Repository 연결 규칙

Custom Repository를 추가할 때는 다음이 모두 연결되어야 한다.

- `RepositoryCustom` 인터페이스 선언
- base Repository의 Custom 인터페이스 상속
- `RepositoryImpl` 구현

### 6-5. 반환 타입 규칙

단건 조회는 `Optional`을 사용하고 `nullable`과 혼용하지 않는다.
다건 조회는 `null` 대신 빈 컬렉션을 반환한다.
존재 여부 확인이 목적이면 `exists` 계열 조회를 우선한다.
개수, 합계, 통계가 목적이면 엔티티 목록 전체를 조회하지 않고 count 또는 집계 쿼리를 사용한다.

---

## 7. 한 줄 요약

- 기본 CRUD는 Spring Data JPA에 둔다.
- 동적 조회, projection, 벌크 수정은 Custom Repository로 분리한다.
- QueryDSL은 `RepositoryImpl`에서만 사용한다.
- JPQL은 고정형 엔티티 조회에만 제한적으로 사용한다.
- 상세 조회는 엔티티 조회 후 매핑한다.
- 목록/검색/배치 조회는 DTO 또는 row projection을 우선한다.
- 비즈니스 계산과 유스케이스 조합은 Service 책임이다.
