package com.lifegrid.controller;

import com.lifegrid.dto.HospitalDto;
import com.lifegrid.service.NetworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
@Tag(name = "Hospitals", description = "Medical centers, specialty capabilities, and real-time bed counts")
public class HospitalController {

    private final NetworkService networkService;

    public HospitalController(NetworkService networkService) {
        this.networkService = networkService;
    }

    @GetMapping
    @Operation(summary = "Get all hospitals with bed capacities and clinical specialties")
    public ResponseEntity<List<HospitalDto>> getAllHospitals() {
        return ResponseEntity.ok(networkService.getAllHospitals());
    }
}
