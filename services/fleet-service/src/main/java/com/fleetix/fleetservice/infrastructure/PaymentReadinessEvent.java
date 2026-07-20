package com.fleetix.fleetservice.infrastructure;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentReadinessEvent(
        UUID requestId,
        String vehicleId,
        String providerId,
        Double estimatedCost,
        OffsetDateTime approveAt
) {}
