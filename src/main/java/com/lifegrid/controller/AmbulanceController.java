package com.lifegrid.controller;

import com.lifegrid.dto.AmbulanceDto;
import com.lifegrid.service.NetworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ambulances")
@Tag(name = "Ambulances", description = "Live fleet tracking and availability status")
public class AmbulanceController {

    private final NetworkService networkService;

    public AmbulanceController(NetworkService networkService) {
        this.networkService = networkService;
    }

    @GetMapping
    @Operation(summary = "Get all ambulance units and statuses")
    public ResponseEntity<List<AmbulanceDto>> getAllAmbulances() {
        return ResponseEntity.ok(networkService.getAllAmbulances());
    }
}
