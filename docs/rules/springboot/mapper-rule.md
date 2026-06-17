# Mapper Rule

## 1. 문서 목적

이 문서는 Java + Spring Boot 프로젝트에서 DTO와 내부 모델 사이의 변환 규칙을 정의한다.
기본 도구는 MapStruct이며, 복잡한 조건부 로직이 필요한 경우에만 수동 매퍼를 허용한다.

---

## 2. 핵심 패턴 요약

- 단순 필드 매핑은 MapStruct `@Mapper` 인터페이스를 기본으로 사용한다.
- 조건부 로직, 집계, 반복 구조가 있는 변환은 수동 `@Component` 매퍼를 사용한다.
- 엔티티 생성은 도메인 Factory 또는 service가 담당하고, 매퍼는 관여하지 않는다.
- 응답 변환은 도메인별 MapStruct 매퍼 인터페이스에서 처리한다.
- 입력 변환과 응답 변환은 매퍼를 분리한다.
- service는 유스케이스 조합과 검증을 담당하고, 매퍼는 필드 이동과 형식 정규화만 담당한다.

---

## 3. MapStruct 기본 설정

### 3-1. 의존성

```groovy
def mapstructVersion = '1.6.3'

implementation "org.mapstruct:mapstruct:${mapstructVersion}"
annotationProcessor "org.mapstruct:mapstruct-processor:${mapstructVersion}"
annotationProcessor "org.projectlombok:lombok-mapstruct-binding:0.2.0"
```

Lombok과 MapStruct를 함께 쓸 때는 `lombok-mapstruct-binding`을 반드시 추가한다.
어노테이션 프로세서 실행 순서를 보장하기 위해서다.

### 3-2. 매퍼 인터페이스 기본 형태

```java
@Mapper(componentModel = "spring")
public interface BidNoticeMapper {
    BidSummaryResponse toSummary(BidNotice notice);
    BidDetailResponse toDetail(BidNotice notice);
}
```

- `componentModel = "spring"` 으로 Spring Bean으로 등록한다.
- 필드명이 일치하면 MapStruct가 자동으로 매핑한다.
- 필드명이 다르면 `@Mapping(source = "...", target = "...")` 으로 지정한다.

### 3-3. 필드명이 다를 때

```java
@Mapper(componentModel = "spring")
public interface BidNoticeMapper {

    @Mapping(source = "externalNoticeId", target = "noticeId")
    @Mapping(source = "agency", target = "organizationName")
    BidSummaryResponse toSummary(BidNotice notice);
}
```

### 3-4. 파생값 계산이 필요할 때

DTO에 단순 파생값(D-Day 계산, 포맷 변환 등)이 필요하면 `@Named` default 메서드로 추가한다.

```java
@Mapper(componentModel = "spring")
public interface BidNoticeMapper {

    @Mapping(source = "bidDeadline", target = "dDay", qualifiedByName = "toDDay")
    BidSummaryResponse toSummary(BidNotice notice);

    @Named("toDDay")
    default long toDDay(LocalDateTime deadline) {
        return ChronoUnit.DAYS.between(LocalDate.now(), deadline.toLocalDate());
    }
}
```

---

## 4. 수동 매퍼를 사용하는 경우

다음 조건 중 하나라도 해당하면 MapStruct 대신 수동 `@Component` 매퍼를 사용한다.

- 조건부 null 체크와 필터링이 복잡하게 얽혀 있는 경우
- 반복 구조(배열, 인덱스 기반 집계)를 처리해야 하는 경우
- 연관된 여러 엔티티를 조합해 하나의 DTO를 구성하는 경우

수동 매퍼는 `@Component`로 등록하고, 변환 방향을 메서드 이름에서 알 수 있게 작성한다.

```java
@Component
public class G2bNoticeMapper {

    public List<BidAttachment> toAttachments(G2bNoticeItem item, BidNotice notice) {
        // 조건부 로직, null 체크 등 MapStruct로 표현하기 어려운 경우
    }
}
```

---

## 5. 엔티티 생성과 매퍼의 경계

엔티티 생성에 매퍼를 사용하지 않는다.

다음 요소가 개입하면 엔티티 생성은 Factory 또는 service가 담당한다.

- 연관 엔티티 참조 필요
- 서버 생성 값 (collectedAt, status 초기값 등)
- 도메인 규칙 적용 필요
- 여러 입력 조합 필요

즉, `BidNotice.create(command)` 같은 정적 팩토리 메서드는 매퍼가 아니라 도메인 생성 행위다.

---

## 6. 응답 변환 규칙

응답 변환은 도메인별 MapStruct 매퍼 인터페이스에 둔다.
repository가 projection DTO를 직접 반환하는 경우 별도 응답 변환을 생략할 수 있다.

### 6-1. null 안전성

- 필수값이 null로 들어오면 MapStruct는 그대로 null을 넣는다.
- 필수값 누락을 즉시 드러내고 싶으면 `@AfterMapping`에서 `Objects.requireNonNull()`로 검증한다.

### 6-2. 도메인 규칙 수준 계산

단순 파생값은 매퍼 내부 `@Named` 메서드에서 계산할 수 있다.
도메인 규칙 수준의 집계 로직은 매퍼에 두지 않고, service에서 계산한 값을 매퍼에 넘긴다.

---

## 7. 입력 변환 규칙 (Query → Criteria)

검색 조건 Query DTO를 내부 Criteria로 변환할 때도 MapStruct를 사용할 수 있다.

이 단계에서 허용되는 작업:
- 필드 이름 치환
- nullable 값 정리
- 조회 경계값 보정

이 단계에서 하지 않는 작업:
- repository 호출
- 비즈니스 규칙 검증
- 상태 판단

---

## 8. Service와 Mapper 책임 경계

### 8-1. Mapper 책임
- 필드 매핑
- 입력 정규화
- 조회 경계값 보정
- 응답 DTO 조립
- 단순 파생값 계산 (`@Named` 메서드)

### 8-2. Service 또는 검증 계층 책임
- repository 호출
- 연관 엔티티 조회
- 비즈니스 유효성 검증
- 유스케이스 조합
- Factory 호출

---

## 9. Projection DTO 규칙

- repository가 projection DTO를 직접 반환하는 경우 별도 응답 변환을 거치지 않을 수 있다.
- projection DTO와 API response DTO는 기본적으로 동일 클래스로 재사용하지 않는다.
- projection DTO는 조회 최적화를 위한 내부 읽기 모델이다.

---

## 10. 변환 방향 분리 원칙

하나의 매퍼 인터페이스는 하나의 변환 방향을 담당한다.

- 입력 변환 매퍼와 응답 변환 매퍼를 동일 인터페이스에 혼합하지 않는다.
- 도메인별로 매퍼를 분리한다 (`BidNoticeMapper`, `UserMapper` 등).

---

## 11. 금지 규칙

- MapStruct 없이 단순 필드 복사를 위한 수동 매퍼를 작성하지 않는다.
- Response DTO에 `from(entity)` 정적 팩토리 메서드를 작성하지 않는다 (변환 로직이 DTO 안에 숨겨진다).
- 매퍼 안에서 repository를 호출하지 않는다.
- 매퍼 안에서 비즈니스 규칙 검증이나 상태 전이를 수행하지 않는다.
- projection DTO를 API response DTO나 쓰기 유스케이스 입력으로 재사용하지 않는다.
- 입력 변환과 응답 변환을 하나의 매퍼 인터페이스에 혼합하지 않는다.

---

## 12. 한 줄 요약

- 단순 매핑은 MapStruct `@Mapper` 인터페이스, 복잡한 조건부 로직은 수동 `@Component` 매퍼.
- 엔티티 생성은 Factory와 service가 담당한다.
- 응답 변환은 도메인별 MapStruct 매퍼 인터페이스에서 처리한다.
- 매퍼는 변환과 정규화만, service는 조회와 검증과 조합만 담당한다.
