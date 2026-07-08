CREATE TABLE tech_tags (
    id         BIGSERIAL    NOT NULL,
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    CONSTRAINT pk_tech_tags PRIMARY KEY (id),
    CONSTRAINT uk_tech_tags_name UNIQUE (name)
);

CREATE TABLE business_areas (
    id         BIGSERIAL    NOT NULL,
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    CONSTRAINT pk_business_areas PRIMARY KEY (id),
    CONSTRAINT uk_business_areas_name UNIQUE (name)
);
