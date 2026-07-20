package com.fleetix.fleetservice.api;


import com.fleetix.fleetservice.application.MaintenanceRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/requests")
@Tag(name = "maintenance-requests", description = "Maintenance request lifecycle")
public class MaintenanceRequestController {

    private final MaintenanceRequestService service;

    public MaintenanceRequestController(MaintenanceRequestService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a maintenance request", description = "Creates a new request with status PENDING.")
    @ApiResponse(responseCode = "201", description = "Request created")
    @ApiResponse(responseCode = "400", description = "Request body failed validation")
    public MaintenanceRequestDto create(@Valid @RequestBody CreateMaintenanceRequestDto request) {
        return MaintenanceRequestDto.from(service.create(request.vehicleId(), request.description()));
    }

    @GetMapping
    @Operation(summary = "List all maintenance requests")
    @ApiResponse(responseCode = "200", description = "All maintenance requests")
    public List<MaintenanceRequestDto> listAll() {
        return service.listAll().stream().map(MaintenanceRequestDto::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a maintenance request by ID")
    @ApiResponse(responseCode = "200", description = "Maintenance request found")
    @ApiResponse(responseCode = "404", description = "Maintenance request not found")
    public MaintenanceRequestDto getById(@PathVariable UUID id) {
        return MaintenanceRequestDto.from(service.getById(id));
    }

    @PatchMapping("/{id}/assign")
    @Operation(summary = "Assign a service provider")
    @ApiResponse(responseCode = "200", description = "Provider assigned; status is now ASSIGNED")
    @ApiResponse(responseCode = "400", description = "Request body failed field validation")
    @ApiResponse(responseCode = "404", description = "Maintenance request not found")
    @ApiResponse(responseCode = "409", description = "Status transition is not valid")
    public MaintenanceRequestDto assign(@PathVariable UUID id, @Valid @RequestBody AssignmentDto request) {
        return MaintenanceRequestDto.from(service.assign(id, request.providerId()));
    }

    @PostMapping("/{id}/estimate")
    @Operation(summary = "Submit inspection findings and repair estimate")
    @ApiResponse(responseCode = "200", description = "Estimate submitted; status is now ESTIMATE_SUBMITTED")
    @ApiResponse(responseCode = "400", description = "Request body failed field validation")
    @ApiResponse(responseCode = "404", description = "Maintenance request not found")
    @ApiResponse(responseCode = "409", description = "Status transition is not valid")
    public MaintenanceRequestDto submitEstimate(@PathVariable UUID id, @Valid @RequestBody EstimateDto request) {
        return MaintenanceRequestDto.from(service.submitEstimate(id, request.findings(), request.estimatedCost()));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a maintenance request")
    @ApiResponse(responseCode = "200", description = "Request approved; status is now APPROVED")
    @ApiResponse(responseCode = "404", description = "Maintenance request not found")
    @ApiResponse(responseCode = "409", description = "Status transition is not valid")
    public MaintenanceRequestDto approve(@PathVariable UUID id,
                                         @Valid @RequestBody(required = false) ApprovalDecisionDto request) {
        return MaintenanceRequestDto.from(service.approve(id, request != null ? request.note() : null));
    }


    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a maintenance request")
    @ApiResponse(responseCode = "200", description = "Request rejected; status is now REJECTED")
    @ApiResponse(responseCode = "404", description = "Maintenance request not found")
    @ApiResponse(responseCode = "409", description = "Status transition is not valid")
    public MaintenanceRequestDto reject(@PathVariable UUID id,
                                         @Valid @RequestBody(required = false) ApprovalDecisionDto request) {
        return MaintenanceRequestDto.from(service.reject(id, request != null ? request.note() : null));
    }

    @PostMapping("/{id}/request-info")
    @Operation(summary = "Request more information on the estimate")
    @ApiResponse(responseCode = "200", description = "More information requested; status is now INFO_REQUESTED")
    @ApiResponse(responseCode = "400", description = "Note is required for information request")
    @ApiResponse(responseCode = "404", description = "Maintenance request not found")
    @ApiResponse(responseCode = "409", description = "Status transition is not valid")
    public MaintenanceRequestDto requestInfo(@PathVariable UUID id,
                                        @Valid @RequestBody ApprovalDecisionDto request) {
        return MaintenanceRequestDto.from(service.requestInfo(id, request.note()));
    }
}
