CREATE TABLE favorites (
    id            BIGSERIAL NOT NULL,
    user_id       BIGINT    NOT NULL,
    bid_notice_id BIGINT    NOT NULL,
    created_at    TIMESTAMP NOT NULL,
    CONSTRAINT pk_favorites PRIMARY KEY (id),
    CONSTRAINT uk_favorites_user_bid UNIQUE (user_id, bid_notice_id),
    CONSTRAINT fk_favorites_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_favorites_bid_notice FOREIGN KEY (bid_notice_id) REFERENCES bid_notices (id)
);
