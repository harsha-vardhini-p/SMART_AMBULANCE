package com.lifegrid.controller;

import com.lifegrid.dto.NodeDto;
import com.lifegrid.dto.RoadDto;
import com.lifegrid.service.NetworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Network", description = "Road network topology, intersection coordinates, and road segments")
public class NetworkController {

    private final NetworkService networkService;

    public NetworkController(NetworkService networkService) {
        this.networkService = networkService;
    }

    @GetMapping("/nodes")
    @Operation(summary = "Get all intersection nodes with geographical coordinates")
    public ResponseEntity<List<NodeDto>> getAllNodes() {
        return ResponseEntity.ok(networkService.getAllNodes());
    }

    @GetMapping("/roads")
    @Operation(summary = "Get all road corridors and distances")
    public ResponseEntity<List<RoadDto>> getAllRoads() {
        return ResponseEntity.ok(networkService.getAllRoads());
    }
}
