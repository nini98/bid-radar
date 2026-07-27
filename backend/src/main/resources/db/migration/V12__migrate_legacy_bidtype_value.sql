-- Issue #19 후속 조치: 프론트/백엔드에서 제거한 오타값 "전자시담(2인 이상)"을
-- 나라장터 명세 값 "전자시담(다자간)"으로 정정. 이 값을 이미 저장한 회사 프로필이 있으면
-- 배열 내 값을 치환한다(치환 결과 중복이 생기면 중복 제거).
UPDATE company_bid_preferences
SET preferred_bid_types = (
    SELECT array_agg(DISTINCT val)
    FROM unnest(
        array_replace(preferred_bid_types, '전자시담(2인 이상)', '전자시담(다자간)')
    ) AS val
)
WHERE '전자시담(2인 이상)' = ANY(preferred_bid_types);
