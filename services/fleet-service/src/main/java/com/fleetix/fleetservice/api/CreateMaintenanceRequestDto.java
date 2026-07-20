package com.fleetix.fleetservice.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMaintenanceRequestDto(
        @NotBlank String vehicleId,
        @NotBlank @Size(max = 1000) String description
) {}
