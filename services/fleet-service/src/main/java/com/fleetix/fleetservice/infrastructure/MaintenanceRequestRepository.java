package com.fleetix.fleetservice.infrastructure;

import com.fleetix.fleetservice.domain.MaintenanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, UUID> {
}
