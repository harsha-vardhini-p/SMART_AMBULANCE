package com.lifegrid.controller;

import com.lifegrid.service.NetworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/simulation")
@Tag(name = "Simulation", description = "Control plane for resetting and reseeding demo simulation state")
public class SimulationController {

    private final NetworkService networkService;

    public SimulationController(NetworkService networkService) {
        this.networkService = networkService;
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset and reseed simulation network",
            description = "Clears active dispatches and emergency records, resets all hospital beds and ambulance locations, and reseeds pristine Vijayawada network.")
    public ResponseEntity<Map<String, Object>> resetSimulation() {
        networkService.resetSimulation();
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "LifeGrid simulation environment successfully reseeded with initial Vijayawada network state."
        ));
    }
}
