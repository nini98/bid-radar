CREATE TABLE company_tech_tags (
    id          BIGSERIAL NOT NULL,
    company_id  BIGINT    NOT NULL,
    tech_tag_id BIGINT    NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    CONSTRAINT pk_company_tech_tags PRIMARY KEY (id),
    CONSTRAINT uk_company_tech_tags_company_tech UNIQUE (company_id, tech_tag_id),
    CONSTRAINT fk_company_tech_tags_company FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT fk_company_tech_tags_tech_tag FOREIGN KEY (tech_tag_id) REFERENCES tech_tags (id)
);

CREATE TABLE company_business_areas (
    id               BIGSERIAL NOT NULL,
    company_id       BIGINT    NOT NULL,
    business_area_id BIGINT    NOT NULL,
    created_at       TIMESTAMP NOT NULL,
    CONSTRAINT pk_company_business_areas PRIMARY KEY (id),
    CONSTRAINT uk_company_business_areas_company_area UNIQUE (company_id, business_area_id),
    CONSTRAINT fk_company_business_areas_company FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT fk_company_business_areas_business_area FOREIGN KEY (business_area_id) REFERENCES business_areas (id)
);

CREATE TABLE company_certificates (
    id                BIGSERIAL    NOT NULL,
    company_id        BIGINT       NOT NULL,
    certificate_name  VARCHAR(200) NOT NULL,
    created_at        TIMESTAMP    NOT NULL,
    CONSTRAINT pk_company_certificates PRIMARY KEY (id),
    CONSTRAINT fk_company_certificates_company FOREIGN KEY (company_id) REFERENCES companies (id)
);

CREATE TABLE company_project_experiences (
    id           BIGSERIAL    NOT NULL,
    company_id   BIGINT       NOT NULL,
    project_type VARCHAR(100),
    description  TEXT,
    created_at   TIMESTAMP    NOT NULL,
    CONSTRAINT pk_company_project_experiences PRIMARY KEY (id),
    CONSTRAINT fk_company_project_experiences_company FOREIGN KEY (company_id) REFERENCES companies (id)
);

CREATE TABLE company_bid_preferences (
    id                        BIGSERIAL NOT NULL,
    company_id                BIGINT    NOT NULL,
    preferred_regions         VARCHAR[],
    budget_min                BIGINT,
    budget_max                BIGINT,
    deadline_min_days         INTEGER,
    preferred_bid_types       VARCHAR[],
    preferred_contract_types  VARCHAR[],
    created_at                TIMESTAMP NOT NULL,
    updated_at                TIMESTAMP NOT NULL,
    CONSTRAINT pk_company_bid_preferences PRIMARY KEY (id),
    CONSTRAINT uk_company_bid_preferences_company_id UNIQUE (company_id),
    CONSTRAINT fk_company_bid_preferences_company FOREIGN KEY (company_id) REFERENCES companies (id)
);
