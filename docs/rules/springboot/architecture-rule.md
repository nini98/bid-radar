# Architecture Rule

## 1. 문서 목적

이 문서는 프로젝트의 상위 구조 원칙과 계층 경계만 정의한다.
세부 구현 규칙은 각 전용 문서를 따른다.

---

## 2. 도메인 중심 패키지 구조

패키지는 기술 계층이 아니라 도메인 기준으로 나눈다.
하나의 도메인 패키지 안에서 필요한 계층을 함께 둔다.

```text
src/main/java/...
├─ {domain}
│  ├─ controller
│  ├─ service
│  ├─ repository
│  ├─ domain
│  └─ dto
└─ common
```

- `controller`, `service`, `repository`를 루트 공용 패키지로 가로 분리하지 않는다.
- 도메인 변경이 한 패키지 단위에서 추적되도록 구성한다.
- 특정 도메인 구현을 `common`이나 다른 도메인 패키지로 흩뜨리지 않는다.
- 어떤 엔티티나 유스케이스가 특정 도메인의 식별자, 상태, 권한 규칙에 강하게 종속되면 별도 최상위 도메인으로 분리하지 않고 그 도메인 패키지 안에 둔다.
- 하위 유스케이스 전용 상태 엔티티나 요청 흐름을 별도 최상위 도메인처럼 승격하지 않는다.

---

## 3. `common` 사용 원칙

`common`에는 공통 기술과 지원 코드만 둔다.

- 허용: 공통 설정, 공통 예외, 공통 응답, 시간/키 생성 유틸, 공통 실행 래퍼, 공통 JPA 기반 코드
- 금지: 특정 도메인 정책, 특정 유스케이스 흐름, 한 도메인에서만 의미가 있는 코드
- 재사용 가능해 보인다는 이유만으로 도메인 코드를 `common`으로 올리지 않는다.

---

## 4. 계층 구조 원칙

기본 흐름은 다음 구조를 따른다.

```text
controller -> service -> repository -> domain
```

- Controller는 HTTP 입출력과 요청 바인딩만 담당한다.
- Service는 유스케이스 조합과 트랜잭션 경계를 담당한다.
- Repository는 영속성 접근만 담당한다.
- Domain은 영속성 모델과 도메인 상태를 표현한다.

상위 계층은 하위 계층을 사용하며, 비즈니스 흐름은 Service를 중심으로 조합한다.

---

## 5. 계층 간 금지 규칙

- Controller는 Repository를 직접 호출하지 않는다.
- Controller는 엔티티를 요청/응답 모델로 직접 사용하지 않는다.
- Controller는 트랜잭션, 영속성, 보안 세부 구현을 직접 다루지 않는다.
- Service는 HTTP 객체, 웹 프레임워크 타입, 요청 바인딩 로직에 의존하지 않는다.
- Service는 SQL, JPQL, QueryDSL 작성 위치가 되지 않는다.
- Repository는 비즈니스 정책, 계산 규칙, 유스케이스 조합을 갖지 않는다.
- Repository는 다른 Repository 호출을 오케스트레이션하지 않는다.
- Domain은 Controller, Repository, 외부 인프라 구현을 알지 않는다.
- `common`은 도메인 정책의 우회 경로가 되지 않는다.

---

## 6. DTO와 내부 객체 분리

외부 API 모델과 내부 처리 객체는 분리한다.

- `dto/request`: 요청 본문 DTO
- `dto/query`: 조회 조건 DTO
- `dto/response`: 응답 DTO
- 내부 처리용 criteria, command, result 객체는 service 내부 패키지에 둔다.
- Request, Query, Response, 내부 객체를 서로 혼용하지 않는다.
- 엔티티를 API DTO로 직접 노출하지 않는다.

---

## 7. 배치 컴포넌트 분리

배치나 장기 실행 작업은 역할별 컴포넌트를 분리한다.

- Scheduler: 진입점
- Service: 실행 흐름 관리
- Processor: 실제 처리 단계 수행
- Runner: 공통 실행 래핑, 로깅, 실패 처리

진입, 흐름 관리, 실제 처리, 공통 실행 책임을 한 클래스에 몰아넣지 않는다.

---

## 8. 세부 규칙 문서

이 문서는 상위 구조만 정의한다. DTO, mapper, repository, transaction, testing, DB migration, config 규칙은 별도 문서를 따른다.
