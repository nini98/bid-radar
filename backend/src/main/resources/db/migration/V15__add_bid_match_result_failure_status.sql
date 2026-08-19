ALTER TABLE bid_match_results
    ALTER COLUMN total_score DROP NOT NULL,
    ALTER COLUMN grade DROP NOT NULL;

ALTER TABLE bid_match_results
    ADD COLUMN status        VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    ADD COLUMN error_message TEXT;

ALTER TABLE bid_match_results
    ALTER COLUMN status DROP DEFAULT;

ALTER TABLE bid_match_results
    ADD CONSTRAINT ck_bid_match_results_status_consistency CHECK (
        (status = 'SUCCESS' AND total_score IS NOT NULL AND grade IS NOT NULL) OR
        (status = 'FAILED' AND total_score IS NULL AND grade IS NULL)
    );
