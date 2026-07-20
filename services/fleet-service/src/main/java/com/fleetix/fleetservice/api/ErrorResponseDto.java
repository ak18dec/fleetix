package com.fleetix.fleetservice.api;

public record ErrorResponseDto(int status, String error, String message) {}
