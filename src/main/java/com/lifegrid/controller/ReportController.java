package com.lifegrid.controller;

import com.lifegrid.dto.SummaryReportDto;
import com.lifegrid.service.NetworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Performance metrics, dispatch averages, and bed occupancy analytics")
public class ReportController {

    private final NetworkService networkService;

    public ReportController(NetworkService networkService) {
        this.networkService = networkService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get overall simulation summary report",
            description = "Returns total dispatches, average response minutes, unassigned emergencies, and hospital bed utilisation.")
    public ResponseEntity<SummaryReportDto> getSummaryReport() {
        return ResponseEntity.ok(networkService.getSummaryReport());
    }
}
