package com.fleetix.fleetservice.api;

import jakarta.validation.constraints.Size;

public record ApprovalDecisionDto(@Size(max = 1000) String note) {}
