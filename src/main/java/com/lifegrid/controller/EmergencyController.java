package com.lifegrid.controller;

import com.lifegrid.dto.CreateEmergencyRequestDto;
import com.lifegrid.dto.EmergencyResponseDto;
import com.lifegrid.service.DispatchService;
import com.lifegrid.service.EmergencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for emergency incident reporting, retrieval, and lifecycle management.
 */
@RestController
@RequestMapping("/api/emergencies")
@Tag(name = "Emergencies", description = "Emergency incident ingestion and dispatch trigger")
public class EmergencyController {

    private final EmergencyService emergencyService;
    private final DispatchService dispatchService;

    public EmergencyController(EmergencyService emergencyService, DispatchService dispatchService) {
        this.emergencyService = emergencyService;
        this.dispatchService = dispatchService;
    }

    @PostMapping
    @Operation(summary = "Report emergency and trigger automated dispatch",
            description = "Creates a new emergency ticket, analyzes priority triage, and dispatches nearest ambulance and best hospital.")
    public ResponseEntity<EmergencyResponseDto> createEmergency(@Valid @RequestBody CreateEmergencyRequestDto request) {
        EmergencyResponseDto response = emergencyService.createAndDispatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all reported emergencies")
    public ResponseEntity<List<EmergencyResponseDto>> getAllEmergencies() {
        return ResponseEntity.ok(emergencyService.getAllEmergencies());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get emergency details by ID")
    public ResponseEntity<EmergencyResponseDto> getEmergencyById(@PathVariable Long id) {
        return ResponseEntity.ok(emergencyService.getEmergencyById(id));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Mark emergency as completed and free ambulance")
    public ResponseEntity<Void> completeEmergency(@PathVariable Long id) {
        dispatchService.completeEmergency(id);
        return ResponseEntity.ok().build();
    }
}
