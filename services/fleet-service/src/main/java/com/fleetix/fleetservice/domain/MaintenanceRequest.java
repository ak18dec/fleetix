package com.fleetix.fleetservice.domain;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "maintenance_requests")
public class MaintenanceRequest {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String vehicleId;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceRequestStatus status;

    private String providerId;

    private OffsetDateTime assignedAt;

    @Column(length = 2000)
    private String findings;

    private Double estimatedCost;
    private OffsetDateTime estimateSubmittedAt;

    @Column(length = 1000)
    private String decisionNote;

    private OffsetDateTime decidedAt;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    public MaintenanceRequest() {}

    public MaintenanceRequest(String vehicleId, String description) {
        this.id = UUID.randomUUID();
        this.vehicleId = vehicleId;
        this.description = description;
        this.status = MaintenanceRequestStatus.PENDING;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getDescription() {
        return description;
    }

    public MaintenanceRequestStatus getStatus() {
        return status;
    }

    public String getProviderId() {
        return providerId;
    }

    public OffsetDateTime getAssignedAt() {
        return assignedAt;
    }

    public String getFindings() {
        return findings;
    }

    public Double getEstimatedCost() {
        return estimatedCost;
    }

    public OffsetDateTime getEstimateSubmittedAt() {
        return estimateSubmittedAt;
    }

    public String getDecisionNote() {
        return decisionNote;
    }

    public OffsetDateTime getDecidedAt() {
        return decidedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(MaintenanceRequestStatus status) {
        this.status = status;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public void setAssignedAt(OffsetDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public void setFindings(String findings) {
        this.findings = findings;
    }

    public void setEstimatedCost(Double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public void setEstimateSubmittedAt(OffsetDateTime estimateSubmittedAt) {
        this.estimateSubmittedAt = estimateSubmittedAt;
    }

    public void setDecisionNote(String decisionNote) {
        this.decisionNote = decisionNote;
    }

    public void setDecidedAt(OffsetDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
