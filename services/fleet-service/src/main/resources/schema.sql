CREATE TABLE IF NOT EXISTS maintenance_requests(
    id                          UUID                        NOT NULL,
    vehicle_id                  VARCHAR(255)                NOT NULL,
    description                 VARCHAR(1000)               NOT NULL,
    status                      VARCHAR(50)                 NOT NULL,
    provider_id                 VARCHAR(255),
    assigned_at                 TIMESTAMP WITH TIME ZONE,
    findings                    VARCHAR(2000),
    estimated_cost              DOUBLE PRECISION,
    estimate_submitted_at       TIMESTAMP WITH TIME ZONE,
    decision_note               VARCHAR(1000),
    decided_at                  TIMESTAMP WITH TIME ZONE,
    created_at                  TIMESTAMP WITH TIME ZONE    NOT NULL,
    updated_at                  TIMESTAMP WITH TIME ZONE    NOT NULL,
    PRIMARY KEY (id)
);