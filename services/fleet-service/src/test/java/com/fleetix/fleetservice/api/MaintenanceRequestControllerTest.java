package com.fleetix.fleetservice.api;

import com.fleetix.fleetservice.application.MaintenanceRequestService;
import com.fleetix.fleetservice.domain.MaintenanceRequest;
import com.fleetix.fleetservice.domain.MaintenanceRequestStatus;
import com.sun.tools.javac.Main;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MaintenanceRequestController.class)
public class MaintenanceRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MaintenanceRequestService service;

    @Test
    void createRequest_returnsCreated() throws Exception {
        MaintenanceRequest entity = new MaintenanceRequest("VH-001", "Brake inspection needed");

        when(service.create("VH-001", "Brake inspection needed"))
                .thenReturn(entity);

        mockMvc.perform(post("/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"vehicleId":"VH-001","description":"Brake inspection needed"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vehicleId").value("VH-001"))
                .andExpect(jsonPath("$.description").value("Brake inspection needed"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createRequest_withBlankVehicleId_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"vehicleId":"","description":"Brake inspection needed"}
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRequest_withMissingDescription_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"vehicleId":"VH-001"}
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listRequests_returnsAll() throws Exception {
        when(service.listAll()).thenReturn(List.of(
                new MaintenanceRequest("VH-001", "Oil change"),
                new MaintenanceRequest("VH-002", "Tyre rotation")
        ));

        mockMvc.perform(get("/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].vehicleId").value("VH-001"))
                .andExpect(jsonPath("$[1].vehicleId").value("VH-002"));
    }

    @Test
    void listRequests_emptyList_returnsOk() throws Exception {
        when(service.listAll()).thenReturn(List.of());
        mockMvc.perform(get("/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getById_knownId_returnsRequest() throws Exception {
        MaintenanceRequest entity = new MaintenanceRequest("VH-003", "Engine check");
        when(service.getById(entity.getId())).thenReturn(entity);

        mockMvc.perform(get("/requests/" + entity.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleId").value("VH-003"));
    }

    @Test
    void getById_unknownId_returnsNotFound() throws Exception {
        UUID unknownId = UUID.randomUUID();
        when(service.getById(unknownId))
                .thenThrow(new NoSuchElementException("Maintenance request not found: " + unknownId));

        mockMvc.perform(get("/requests/" + unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void assign_pendingRequest_returnsAssigned() throws Exception {
        MaintenanceRequest entity = new MaintenanceRequest("VH-001", "Brake inspection needed");
        entity.setStatus(MaintenanceRequestStatus.ASSIGNED);
        entity.setProviderId("PROV-01");
        entity.setAssignedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());
        when(service.assign(entity.getId(), "PROV-01")).thenReturn(entity);

        mockMvc.perform(patch("/requests/" + entity.getId() + "/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"providerId":"PROV-01"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.providerId").value("PROV-01"));

    }

    @Test
    void assign_nonPendingRequest_returnsConflict() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.assign(id, "PROV-01"))
                .thenThrow(new IllegalStateException("Cannot assign provider: request status is ASSIGNED"));

        mockMvc.perform(patch("/requests/" + id + "/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"providerId":"PROV-01"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void assign_withBlankProviderId_returnsBadRequest() throws Exception {
        mockMvc.perform(patch("/requests/" + UUID.randomUUID() + "/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"providerId":""}
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitEstimate_assignedRequest_returnsEstimateSubmitted() throws Exception {
        MaintenanceRequest entity = new MaintenanceRequest("VH-001", "Brake inspection needed");
        entity.setStatus(MaintenanceRequestStatus.ESTIMATE_SUBMITTED);
        entity.setFindings("Brake pads worn out");
        entity.setEstimatedCost(250.0);
        entity.setEstimateSubmittedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());
        when(service.submitEstimate(entity.getId(), "Brake pads worn out", 250.0)).thenReturn(entity);

        mockMvc.perform(post("/requests/" + entity.getId() + "/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"findings":"Brake pads worn out", "estimatedCost":250.0}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ESTIMATE_SUBMITTED"))
                .andExpect(jsonPath("$.findings").value("Brake pads worn out"))
                .andExpect(jsonPath("$.estimatedCost").value(250.0));
    }

    @Test
    void submitEstimate_nonAssignedRequest_returnsConflict() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.submitEstimate(id, "Findings", 100.0))
                .thenThrow(new IllegalStateException("Cannot submit estimate: request status is PENDING"));

        mockMvc.perform(post("/requests/" + id + "/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"findings":"Findings", "estimatedCost":100.0}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void submitEstimate_withBlankFindings_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/requests/" + UUID.randomUUID() + "/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"findings":"", "estimatedCost":100.0}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approve_estimateSubmittedRequest_returnsApproved() throws Exception {
        MaintenanceRequest entity = new MaintenanceRequest("VH-001", "Brake inspection");
        entity.setStatus(MaintenanceRequestStatus.APPROVED);
        entity.setDecisionNote("Looks good");
        entity.setDecidedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());
        when(service.approve(entity.getId(), "Looks good"))
                .thenReturn(entity);

        mockMvc.perform(post("/requests/" + entity.getId() + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"note":"Looks good"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.decisionNote").value("Looks good"));
    }

    @Test
    void approve_nonEstimateSubmittedRequest_returnsConflict() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.approve(id, null))
                .thenThrow(new IllegalStateException("Cannot approve: request status is PENDING"));

        mockMvc.perform(post("/requests/" + id + "/approve"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void reject_estimateSubmittedRequest_returnsRejected() throws Exception {
        MaintenanceRequest entity = new MaintenanceRequest("VH-001", "Brake inspection");
        entity.setStatus(MaintenanceRequestStatus.REJECTED);
        entity.setDecisionNote("Cost too high");
        entity.setDecidedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());
        when(service.approve(entity.getId(), "Cost too high"))
                .thenReturn(entity);

        mockMvc.perform(post("/requests/" + entity.getId() + "/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"note":"Cost too high"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.decisionNote").value("Cost too high"));
    }

    @Test
    void reject_nonEstimateSubmittedRequest_returnsConflict() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.reject(id, null))
                .thenThrow(new IllegalStateException("Cannot reject: request status is PENDING"));

        mockMvc.perform(post("/requests/" + id + "/reject"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void requestInfo_estimateSubmittedRequest_returnsInfoRequested() throws Exception {
        MaintenanceRequest entity = new MaintenanceRequest("VH-001", "Brake inspection");
        entity.setStatus(MaintenanceRequestStatus.INFO_REQUESTED);
        entity.setDecisionNote("Please provide parts breakdown");
        entity.setDecidedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());
        when(service.requestInfo(entity.getId(), "Please provide parts breakdown"))
                .thenReturn(entity);

        mockMvc.perform(post("/requests/" + entity.getId() + "/request-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"note":"Please provide parts breakdown"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INFO_REQUESTED"))
                .andExpect(jsonPath("$.decisionNote").value("Please provide parts breakdown"));
    }

    @Test
    void requestInfo_nonEstimateSubmittedRequest_returnsConflict() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.requestInfo(id, "Need more info"))
                .thenThrow(new IllegalStateException("Cannot reject: request status is PENDING"));

        mockMvc.perform(post("/requests/" + id + "/request-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"note":"Need more info"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void requestInfo_withBlankNote_returnsBadRequest() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.requestInfo(id, ""))
                .thenThrow(new IllegalArgumentException("note must not be blank for information request"));

        mockMvc.perform(post("/requests/" + id + "/request-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"note":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
