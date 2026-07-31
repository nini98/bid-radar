CREATE TABLE match_calculation_status (
    id         BIGSERIAL    NOT NULL,
    company_id BIGINT       NOT NULL,
    status     VARCHAR(20)  NOT NULL,
    lock_token VARCHAR(36)  NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL,
    CONSTRAINT pk_match_calculation_status PRIMARY KEY (id),
    CONSTRAINT uk_match_calculation_status_company UNIQUE (company_id),
    CONSTRAINT fk_match_calculation_status_company FOREIGN KEY (company_id) REFERENCES companies (id)
);
