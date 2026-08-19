-- Codex 리뷰(PR #54): V15의 CHECK 제약이 total_score/grade만 검증해서, FAILED인데
-- score_tech/score_region/score_budget/score_business/matched_keywords/score_reason이
-- 남아있는 모순된 row도 저장을 막지 못했다. markFailed()는 이 필드들을 전부 null로 지우므로
-- 제약도 동일한 범위로 넓힌다.
ALTER TABLE bid_match_results
    DROP CONSTRAINT ck_bid_match_results_status_consistency;

ALTER TABLE bid_match_results
    ADD CONSTRAINT ck_bid_match_results_status_consistency CHECK (
        (status = 'SUCCESS' AND total_score IS NOT NULL AND grade IS NOT NULL) OR
        (status = 'FAILED' AND total_score IS NULL AND grade IS NULL
            AND score_tech IS NULL AND score_region IS NULL
            AND score_budget IS NULL AND score_business IS NULL
            AND matched_keywords IS NULL AND score_reason IS NULL)
    );
