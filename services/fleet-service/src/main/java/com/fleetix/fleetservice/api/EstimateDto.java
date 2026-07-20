package com.fleetix.fleetservice.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record EstimateDto(
   @NotBlank @Size(max = 2000) String findings,
   @NotNull @PositiveOrZero Double estimatedCost
) {}
