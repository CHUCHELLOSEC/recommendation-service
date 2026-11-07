--liquibase formatted sql

--changeset chuchelo:1
CREATE TABLE dynamic_rule (
                              id           UUID  PRIMARY KEY,
                              product_name TEXT  NOT NULL,
                              product_id   UUID  NOT NULL,
                              product_text TEXT  NOT NULL,
                              rule         JSONB NOT NULL
);
--rollback DROP TABLE dynamic_rule;

--changeset chuchelo:2
CREATE INDEX idx_dynamic_rule_product_id ON dynamic_rule (product_id);
--rollback DROP INDEX idx_dynamic_rule_product_id;