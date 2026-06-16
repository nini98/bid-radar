# PostgreSQL Rule

## 1. 문서 목적
- 이 프로젝트는 PostgreSQL을 전용 DB로 사용한다.
- PostgreSQL 특유의 타입, 인덱스, 기능을 활용할 때 지켜야 할 규칙을 정의한다.

---

## 2. JSONB 컬럼 매핑

- JSONB 컬럼은 엔티티에서 `String` 타입으로 매핑한다.
- `@Column(columnDefinition = "jsonb")`만으로는 부족하다. Hibernate가 JDBC 파라미터를 `VARCHAR`로 바인딩해 PostgreSQL이 타입 불일치 오류를 낸다.
- 반드시 `@JdbcTypeCode(SqlTypes.JSON)`을 함께 선언한다.

```java
@JdbcTypeCode(SqlTypes.JSON)
@Column(name = "raw_data", columnDefinition = "jsonb")
private String rawData;
```

- Flyway 마이그레이션에서 컬럼을 `JSONB`로 선언했다면, 대응하는 엔티티 필드에 위 두 어노테이션이 반드시 있어야 한다.

---

## 3. (추가 예정)

향후 아래 주제에 대한 규칙을 이 파일에 추가한다.

- GIN 인덱스 (JSONB 쿼리 최적화)
- PostgreSQL 전문 검색 (`tsvector`, `tsquery`)
- `@>` 연산자를 활용한 JSONB 필터 쿼리
