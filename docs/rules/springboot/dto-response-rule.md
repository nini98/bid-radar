# Response DTO 네이밍 & 설계 규칙

## 1. 문서 목적

이 문서는 Response DTO의 네이밍, 역할, 위치에 대한 공통 규칙을 정의한다.
목표는 API 응답 구조를 일관되게 유지하고, 목록/상세 분리와 정보 노출 범위를 함께 통제하는 것이다.

---

## 2. 기본 설계 원칙

- 엔티티는 외부로 직접 노출하지 않는다.
- Controller와 Service의 반환 타입은 항상 Response DTO를 사용한다.
- 목록/검색 응답과 단건 상세 응답은 반드시 분리한다.
- DTO 이름에는 용도와 컨텍스트가 드러나야 한다.

---

## 3. 네이밍 규칙

### Summary / Detail 패턴

- `Summary`: 목록, 검색 API에서 사용하며 한 줄(row) 수준의 최소 정보만 담는다.
- `Detail`: 단건 조회 API에서 사용하며 상세 화면 하나를 구성할 수 있는 정보 집합을 담는다.

### 표준 네이밍

- `{Domain}SummaryResponse`
- `{Domain}DetailResponse`
- 하위 구성요소는 `{Domain}Response`

예:
- `BidSummaryResponse`
- `BidDetailResponse`
- `BidAnalysisResponse`

---

## 4. DTO 설계 규칙

- Summary DTO에는 불필요한 개인정보와 상세 정보를 넣지 않는다.
- 목록 화면에 필요한 최소 식별 정보만 포함한다.
- Detail DTO는 해당 리소스의 상세 화면을 구성할 수 있어야 한다.
- Summary DTO와 Detail DTO는 서로 재사용하지 않는다.
- 하위 구성요소는 독립 API가 생기기 전까지 별도 Summary/Detail로 과도하게 분리하지 않는다.

---

## 5. 중첩 DTO 사용 원칙

- DTO 내부에 DTO를 중첩하는 것은 허용한다.
- Summary DTO 안에는 Summary DTO만 중첩한다.
- Detail DTO 안에서는 Detail DTO 또는 하위 Response DTO를 사용할 수 있다.
- 단건 상세 화면 구성 범위를 넘는 과도한 중첩은 지양한다.

---

## 6. 목록/상세 조회 기준

### 목록/검색 API

- Summary DTO를 사용한다.
- `items` 같은 하위 컬렉션은 기본적으로 포함하지 않는다.

### 단건 상세 API

- Detail DTO를 사용한다.
- 상세 화면 구성에 필요한 하위 구성요소를 포함할 수 있다.

---

## 7. DTO 위치 규칙

- Response DTO는 해당 API를 제공하는 주체 도메인의 `dto/response`에 둔다.
- 소속 기준은 "필드가 어느 엔티티를 담는가"보다 "어느 API 응답 맥락에 속하는가"를 우선한다.
- 하위 구성요소 DTO도 독립 API가 없으면 상위 API 도메인의 `dto/response`에 함께 둔다.
- 공통 API 래퍼 응답은 `common.response`에 둔다.
- 재사용 가능성이 보여도 독립 조회 맥락이 생기기 전까지는 공통 패키지로 승격하지 않는다.

---

## 8. 변환 방식 규칙

Response DTO는 MapStruct `@Mapper` 인터페이스를 통해 변환한다. (`mapper-rule.md` 참조)

- Response DTO 내부에 `from(entity)` 정적 팩토리 메서드를 작성하지 않는다.
- 변환 로직은 매퍼 인터페이스에 두고, DTO는 순수 데이터 구조로 유지한다.
- Java record 또는 Lombok `@Value`로 불변 DTO를 설계한다.

```java
// 금지: DTO 안에 변환 로직
public record BidSummaryResponse(...) {
    public static BidSummaryResponse from(BidNotice notice) { ... } // 금지
}

// 권장: MapStruct 매퍼에서 변환
@Mapper(componentModel = "spring")
public interface BidNoticeMapper {
    BidSummaryResponse toSummary(BidNotice notice);
}
```

---

## 9. 핵심 규칙

- 목록은 `Summary`, 상세는 `Detail`로 분리한다.
- Summary에는 최소 정보만 담고, Detail에 상세 구성을 맡긴다.
- Summary 안에는 Summary만 중첩한다.
- 엔티티를 직접 응답으로 노출하지 않는다.
- Response DTO는 API 응답 컨텍스트 기준으로 배치한다.
- DTO는 순수 데이터 구조로 유지하고, 변환 로직은 MapStruct 매퍼에 둔다.
