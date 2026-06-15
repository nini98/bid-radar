CREATE TABLE bid_attachments (
    id            BIGSERIAL    NOT NULL,
    bid_notice_id BIGINT       NOT NULL,
    file_name     VARCHAR(500) NOT NULL,
    file_type     VARCHAR(20),
    file_size     BIGINT,
    s3_key        TEXT,
    download_url  TEXT,
    is_downloaded BOOLEAN      NOT NULL DEFAULT FALSE,
    is_analyzed   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL,
    CONSTRAINT pk_bid_attachments PRIMARY KEY (id),
    CONSTRAINT fk_bid_attachments_bid_notice FOREIGN KEY (bid_notice_id) REFERENCES bid_notices (id)
);

CREATE INDEX idx_bid_attachments_bid_notice_id ON bid_attachments (bid_notice_id);
