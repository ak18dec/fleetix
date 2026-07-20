package com.fleetix.fleetservice.api;

import com.fleetix.fleetservice.domain.MaintenanceRequest;
import com.fleetix.fleetservice.domain.MaintenanceRequestStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MaintenanceRequestDto(
        UUID id,
        String vehicleId,
        String description,
        MaintenanceRequestStatus status,
        String providerId,
        OffsetDateTime assignedAt,
        String findings,
        Double estimatedCost,
        OffsetDateTime estimateSubmittedAt,
        String decisionNote,
        OffsetDateTime decidedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static MaintenanceRequestDto from(MaintenanceRequest entity) {
        return new MaintenanceRequestDto(
                entity.getId(),
                entity.getVehicleId(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getProviderId(),
                entity.getAssignedAt(),
                entity.getFindings(),
                entity.getEstimatedCost(),
                entity.getEstimateSubmittedAt(),
                entity.getDecisionNote(),
                entity.getDecidedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
