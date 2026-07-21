package com.fleetix.fleetservice.application;

import com.fleetix.fleetservice.domain.MaintenanceRequest;
import com.fleetix.fleetservice.domain.MaintenanceRequestStatus;
import com.fleetix.fleetservice.infrastructure.MaintenanceRequestRepository;
import com.fleetix.fleetservice.infrastructure.PaymentReadinessEvent;
import com.fleetix.fleetservice.infrastructure.PaymentReadinessEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MaintenanceRequestServiceTest {

    @Mock
    private MaintenanceRequestRepository repository;

    @Mock
    private PaymentReadinessEventPublisher eventPublisher;

    @InjectMocks
    private MaintenanceRequestService service;

    @Test
    void approve_publishesPaymentReadinessEvent() {
        MaintenanceRequest entity = new MaintenanceRequest("VH-001", "Brake inspection");
        entity.setProviderId("PROV-01");
        entity.setEstimatedCost(250.0);
        entity.setStatus(MaintenanceRequestStatus.ESTIMATE_SUBMITTED);
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenReturn(entity);
        service.approve(entity.getId(), "Looks good");

        ArgumentCaptor<PaymentReadinessEvent> captor = ArgumentCaptor.forClass(PaymentReadinessEvent.class);
        verify(eventPublisher).publish(captor.capture());
        PaymentReadinessEvent event = captor.getValue();

        assertThat(event.requestId()).isEqualTo(entity.getId());
        assertThat(event.vehicleId()).isEqualTo("VH-001");
        assertThat(event.providerId()).isEqualTo("PROV-01");
        assertThat(event.estimatedCost()).isEqualTo(250.0);
    }

    @Test
    void reject_doesNotPublishEvent() {
        MaintenanceRequest entity = new MaintenanceRequest("VH-001", "Brake inspection");
        entity.setStatus(MaintenanceRequestStatus.ESTIMATE_SUBMITTED);
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        service.reject(entity.getId(), "Cost too high");
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void requestInfo_doesNotPublishEvent() {
        MaintenanceRequest entity = new MaintenanceRequest("VH-001", "Brake inspection");
        entity.setStatus(MaintenanceRequestStatus.ESTIMATE_SUBMITTED);
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenReturn(entity);

        service.requestInfo(entity.getId(), "Please provide parts breakdown");
        verifyNoInteractions(eventPublisher);
    }
}
