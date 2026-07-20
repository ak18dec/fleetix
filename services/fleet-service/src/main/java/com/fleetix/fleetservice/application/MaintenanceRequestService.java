package com.fleetix.fleetservice.application;

import com.fleetix.fleetservice.domain.MaintenanceRequest;
import com.fleetix.fleetservice.domain.MaintenanceRequestStatus;
import com.fleetix.fleetservice.infrastructure.MaintenanceRequestRepository;
import com.fleetix.fleetservice.infrastructure.PaymentReadinessEvent;
import com.fleetix.fleetservice.infrastructure.PaymentReadinessEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class MaintenanceRequestService {

    private final MaintenanceRequestRepository repository;
    private final PaymentReadinessEventPublisher eventPublisher;

    public MaintenanceRequestService(MaintenanceRequestRepository repository, PaymentReadinessEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public MaintenanceRequest create(String vehicleId, String description) {
        return repository.save(new MaintenanceRequest(vehicleId, description));
    }

    @Transactional(readOnly = true)
    public List<MaintenanceRequest> listAll() {
        return repository.findAll();
    }

    @Transactional
    public MaintenanceRequest getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Maintenance request not found: " + id));
    }

    @Transactional
    public MaintenanceRequest assign(UUID id, String providerId) {
        MaintenanceRequest request = getById(id);
        if(request.getStatus() != MaintenanceRequestStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot assign provider: request status is " + request.getStatus()
            );
        }
        request.setProviderId(providerId);
        request.setAssignedAt(OffsetDateTime.now());
        request.setStatus(MaintenanceRequestStatus.ASSIGNED);
        request.setUpdatedAt(OffsetDateTime.now());
        return repository.save(request);
    }

    @Transactional
    public MaintenanceRequest submitEstimate(UUID id, String findings, Double estimatedCost) {
        MaintenanceRequest request = getById(id);
        if(request.getStatus() != MaintenanceRequestStatus.ASSIGNED) {
            throw new IllegalStateException(
                    "Cannot submit estimate: request status is " + request.getStatus()
            );
        }
        request.setFindings(findings);
        request.setEstimatedCost(estimatedCost);
        request.setEstimateSubmittedAt(OffsetDateTime.now());
        request.setStatus(MaintenanceRequestStatus.ESTIMATE_SUBMITTED);
        request.setUpdatedAt(OffsetDateTime.now());
        return repository.save(request);
    }

    @Transactional
    public MaintenanceRequest approve(UUID id, String note) {
        MaintenanceRequest request = getById(id);
        if(request.getStatus() != MaintenanceRequestStatus.ESTIMATE_SUBMITTED) {
            throw new IllegalStateException(
                    "Cannot approve: request status is " + request.getStatus()
            );
        }
        request.setDecisionNote(note);
        request.setDecidedAt(OffsetDateTime.now());
        request.setStatus(MaintenanceRequestStatus.APPROVED);
        request.setUpdatedAt(OffsetDateTime.now());
        MaintenanceRequest saved = repository.save(request);
        eventPublisher.publish(new PaymentReadinessEvent(
                saved.getId(),
                saved.getVehicleId(),
                saved.getProviderId(),
                saved.getEstimatedCost(),
                saved.getDecidedAt()
        ));
        return saved;
    }

    @Transactional
    public MaintenanceRequest reject(UUID id, String note) {
        MaintenanceRequest request = getById(id);
        if(request.getStatus() != MaintenanceRequestStatus.ESTIMATE_SUBMITTED) {
            throw new IllegalStateException(
                    "Cannot reject: request status is " + request.getStatus()
            );
        }
        request.setDecisionNote(note);
        request.setDecidedAt(OffsetDateTime.now());
        request.setStatus(MaintenanceRequestStatus.REJECTED);
        request.setUpdatedAt(OffsetDateTime.now());
        return repository.save(request);
    }

    @Transactional
    public MaintenanceRequest requestInfo(UUID id, String note) {
        if(note == null || note.isBlank()) {
            throw new IllegalArgumentException(
                    "note must not be blank for information request"
            );
        }
        MaintenanceRequest request = getById(id);
        if(request.getStatus() != MaintenanceRequestStatus.ESTIMATE_SUBMITTED) {
            throw new IllegalStateException(
                    "Cannot request info: request status is " + request.getStatus()
            );
        }
        request.setDecisionNote(note);
        request.setDecidedAt(OffsetDateTime.now());
        request.setStatus(MaintenanceRequestStatus.INFO_REQUESTED);
        request.setUpdatedAt(OffsetDateTime.now());
        return repository.save(request);
    }
}
