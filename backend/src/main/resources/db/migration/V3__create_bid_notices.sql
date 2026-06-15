CREATE TABLE bid_notices (
    id                    BIGSERIAL    NOT NULL,
    external_notice_id    VARCHAR(100) NOT NULL,
    source                VARCHAR(50)  NOT NULL,
    title                 VARCHAR(500) NOT NULL,
    agency                VARCHAR(200),
    budget                BIGINT,
    region                VARCHAR(100),
    bid_type              VARCHAR(50),
    contract_type         VARCHAR(50),
    industry_restriction  VARCHAR(100),
    region_restriction    VARCHAR(100),
    qualification_summary TEXT,
    notice_url            TEXT,
    published_at          TIMESTAMP,
    question_deadline     TIMESTAMP,
    document_deadline     TIMESTAMP,
    bid_deadline          TIMESTAMP,
    open_at               TIMESTAMP,
    status                VARCHAR(20)  NOT NULL,
    raw_data              JSONB,
    collected_at          TIMESTAMP,
    created_at            TIMESTAMP    NOT NULL,
    updated_at            TIMESTAMP    NOT NULL,
    CONSTRAINT pk_bid_notices PRIMARY KEY (id),
    CONSTRAINT uk_bid_notices_external_notice_id UNIQUE (external_notice_id)
);

CREATE INDEX idx_bid_notices_bid_deadline  ON bid_notices (bid_deadline);
CREATE INDEX idx_bid_notices_published_at  ON bid_notices (published_at);
CREATE INDEX idx_bid_notices_status        ON bid_notices (status);
CREATE INDEX idx_bid_notices_agency        ON bid_notices (agency);
CREATE INDEX idx_bid_notices_region        ON bid_notices (region);
