package com.lifegrid.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for reporting a new emergency incident.
 */
public class CreateEmergencyRequestDto {

    @NotBlank(message = "Emergency incident type is required (e.g., CARDIAC_ARREST, TRAUMA_ACCIDENT)")
    private String type;

    @Min(value = 1, message = "Severity score must be at least 1 (minor)")
    @Max(value = 10, message = "Severity score cannot exceed 10 (critical)")
    private int severity;

    @NotNull(message = "Location node ID (intersection) is required")
    private Long locationNodeId;

    public CreateEmergencyRequestDto() {
    }

    public CreateEmergencyRequestDto(String type, int severity, Long locationNodeId) {
        this.type = type;
        this.severity = severity;
        this.locationNodeId = locationNodeId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public Long getLocationNodeId() {
        return locationNodeId;
    }

    public void setLocationNodeId(Long locationNodeId) {
        this.locationNodeId = locationNodeId;
    }
}
