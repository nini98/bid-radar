CREATE TABLE bid_match_results (
    id               BIGSERIAL     NOT NULL,
    bid_notice_id    BIGINT        NOT NULL,
    company_id       BIGINT        NOT NULL,
    total_score      NUMERIC(5,2)  NOT NULL,
    grade            VARCHAR(20)   NOT NULL,
    score_tech       NUMERIC(5,2),
    score_region     NUMERIC(5,2),
    score_budget     NUMERIC(5,2),
    score_business   NUMERIC(5,2),
    matched_keywords JSONB,
    score_reason     TEXT,
    calculated_at    TIMESTAMP     NOT NULL,
    created_at       TIMESTAMP     NOT NULL,
    CONSTRAINT pk_bid_match_results PRIMARY KEY (id),
    CONSTRAINT uk_bid_match_results_notice_company UNIQUE (bid_notice_id, company_id),
    CONSTRAINT fk_bid_match_results_bid_notice FOREIGN KEY (bid_notice_id) REFERENCES bid_notices (id),
    CONSTRAINT fk_bid_match_results_company FOREIGN KEY (company_id) REFERENCES companies (id)
);

CREATE INDEX idx_bid_match_results_company_id ON bid_match_results (company_id);
