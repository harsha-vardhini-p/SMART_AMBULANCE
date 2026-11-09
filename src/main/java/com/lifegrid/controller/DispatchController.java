package com.lifegrid.controller;

import com.lifegrid.dto.DispatchLogDto;
import com.lifegrid.service.NetworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dispatches")
@Tag(name = "Dispatches", description = "Audit logs and historical routing decisions")
public class DispatchController {

    private final NetworkService networkService;

    public DispatchController(NetworkService networkService) {
        this.networkService = networkService;
    }

    @GetMapping
    @Operation(summary = "Get all dispatch execution audit logs")
    public ResponseEntity<List<DispatchLogDto>> getAllDispatches() {
        return ResponseEntity.ok(networkService.getAllDispatches());
    }
}
