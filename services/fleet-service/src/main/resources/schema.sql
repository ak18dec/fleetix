CREATE TABLE IF NOT EXISTS maintenance_requests(
    id,
    vehicle_id,
    description,
    status,
    provider_id,
    assigned_at,
    findings,
    estimated_cost,
    estimate_submitted_at,
    decision_note,
    decided_at,
    created_at,
    updated_at,
    PRIMARY KEY (id)

)