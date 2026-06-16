# External API Rule

## 1. 문서 목적
- 외부 API(공공데이터포털, 외부 서비스 등)를 연동할 때 발생하는 공통 문제와 대응 패턴을 정의한다.
- 컴포넌트 분리 원칙은 `architecture-rule.md` Section 7을 따른다.

---

## 2. 응답 필드 길이 방어

외부 API의 응답 필드는 DB 컬럼 길이를 초과할 수 있다. API 명세의 최대 길이와 DB 컬럼 길이가 다를 경우 저장 시 오류가 발생한다.

- 외부 API 응답을 엔티티에 매핑할 때 String 필드는 저장 전에 컬럼 길이 기준으로 잘라낸다.
- truncate 처리는 Processor 계층(저장 직전)에서 수행한다.

```java
private static String truncate(String s, int maxLength) {
    if (s == null) return null;
    return s.length() <= maxLength ? s : s.substring(0, maxLength);
}
```

---

## 3. 건별 예외 처리

배치 수집 시 단일 항목 실패가 전체 배치를 중단시키지 않도록 항목 단위로 예외를 처리한다.

- 개별 항목 저장은 `@Transactional` 단위로 수행한다.
- Service 오케스트레이션 계층에서 항목 단위로 try-catch해 실패 로그를 남기고 다음 항목을 계속 처리한다.

```java
for (Item item : items) {
    try {
        processor.process(item);
    } catch (Exception e) {
        log.error("항목 처리 실패: id={}", item.getId(), e);
    }
}
```

---

## 4. Pre-encoded 파라미터 처리

일부 API는 서비스 키나 토큰을 URL 인코딩된 값(Encoding key)으로 제공한다. Spring의 `UriComponentsBuilder`나 `RestClient`의 URI 빌더는 이미 인코딩된 값을 재인코딩해 인증 오류를 낸다.

- URL을 문자열로 직접 조합한 뒤 `URI.create(url)`로 전달한다.
- `UriComponentsBuilder.queryParam()`이나 람다 URI 빌더를 사용하지 않는다.

```java
String url = baseUrl + operation
        + "?ServiceKey=" + encodedKey   // 이미 인코딩된 키를 그대로 사용
        + "&type=json&pageNo=" + pageNo;

restClient.get().uri(URI.create(url)).retrieve().body(String.class);
```

---

## 5. 응답 스키마 실측 확인

API 문서(특히 XML 기반 문서)의 응답 구조와 실제 JSON 응답이 다를 수 있다.

- JSON 응답은 반드시 실제 호출로 구조를 확인한 뒤 파싱 코드를 작성한다.
- 배열 여부, 중간 키 존재 여부 등을 문서만으로 가정하지 않는다.

```java
// 문서상 <items><item> 구조이더라도 JSON은 body.items[] 직접 배열일 수 있다
JsonNode itemsNode = body.path("items");
if (!itemsNode.isArray()) return emptyResult;
```
