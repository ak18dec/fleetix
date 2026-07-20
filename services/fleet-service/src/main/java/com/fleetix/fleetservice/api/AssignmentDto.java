package com.fleetix.fleetservice.api;

import jakarta.validation.constraints.NotBlank;

public record AssignmentDto(@NotBlank String providerId) {}
