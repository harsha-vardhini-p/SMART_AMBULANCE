package com.lifegrid.dto;

import com.lifegrid.domain.Emergency;
import com.lifegrid.domain.EmergencyStatus;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing emergency ticket status and allocation results.
 */
public class EmergencyResponseDto {

    private Long id;
    private String type;
    private int severity;
    private Long locationNodeId;
    private EmergencyStatus status;
    private LocalDateTime reportedAt;
    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;
    private Long ambulanceId;
    private Long hospitalId;
    private Double estimatedMinutes;
    private DispatchResponseDto dispatchDetails;

    public EmergencyResponseDto() {
    }

    public static EmergencyResponseDto fromEntity(Emergency emergency) {
        if (emergency == null) return null;
        EmergencyResponseDto dto = new EmergencyResponseDto();
        dto.setId(emergency.getId());
        dto.setType(emergency.getType());
        dto.setSeverity(emergency.getSeverity());
        dto.setLocationNodeId(emergency.getLocationNodeId());
        dto.setStatus(emergency.getStatus());
        dto.setReportedAt(emergency.getReportedAt());
        dto.setAssignedAt(emergency.getAssignedAt());
        dto.setCompletedAt(emergency.getCompletedAt());
        dto.setAmbulanceId(emergency.getAmbulanceId());
        dto.setHospitalId(emergency.getHospitalId());
        dto.setEstimatedMinutes(emergency.getEstimatedMinutes());
        return dto;
    }

    public static EmergencyResponseDto fromEntityWithDispatch(Emergency emergency, DispatchResponseDto dispatchDetails) {
        EmergencyResponseDto dto = fromEntity(emergency);
        if (dto != null) {
            dto.setDispatchDetails(dispatchDetails);
        }
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public EmergencyStatus getStatus() {
        return status;
    }

    public void setStatus(EmergencyStatus status) {
        this.status = status;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(LocalDateTime reportedAt) {
        this.reportedAt = reportedAt;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Long getAmbulanceId() {
        return ambulanceId;
    }

    public void setAmbulanceId(Long ambulanceId) {
        this.ambulanceId = ambulanceId;
    }

    public Long getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId) {
        this.hospitalId = hospitalId;
    }

    public Double getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(Double estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public DispatchResponseDto getDispatchDetails() {
        return dispatchDetails;
    }

    public void setDispatchDetails(DispatchResponseDto dispatchDetails) {
        this.dispatchDetails = dispatchDetails;
    }
}
