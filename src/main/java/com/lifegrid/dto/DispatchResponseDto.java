package com.lifegrid.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object encapsulating complete dispatch execution details.
 */
public class DispatchResponseDto {

    private Long emergencyId;
    private String emergencyType;
    private int severity;
    private Long locationNodeId;
    private Long ambulanceId;
    private String ambulanceCallSign;
    private String ambulanceType;
    private Long hospitalId;
    private String hospitalName;
    private double totalDistanceKm;
    private double totalEstimatedMinutes;
    private List<Long> routeNodeIds;
    private boolean fallbackFacilityApplied;
    private LocalDateTime assignedAt;

    public DispatchResponseDto() {
    }

    public DispatchResponseDto(Long emergencyId,
                               String emergencyType,
                               int severity,
                               Long locationNodeId,
                               Long ambulanceId,
                               String ambulanceCallSign,
                               String ambulanceType,
                               Long hospitalId,
                               String hospitalName,
                               double totalDistanceKm,
                               double totalEstimatedMinutes,
                               List<Long> routeNodeIds,
                               boolean fallbackFacilityApplied,
                               LocalDateTime assignedAt) {
        this.emergencyId = emergencyId;
        this.emergencyType = emergencyType;
        this.severity = severity;
        this.locationNodeId = locationNodeId;
        this.ambulanceId = ambulanceId;
        this.ambulanceCallSign = ambulanceCallSign;
        this.ambulanceType = ambulanceType;
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
        this.totalDistanceKm = totalDistanceKm;
        this.totalEstimatedMinutes = totalEstimatedMinutes;
        this.routeNodeIds = routeNodeIds;
        this.fallbackFacilityApplied = fallbackFacilityApplied;
        this.assignedAt = assignedAt;
    }

    public Long getEmergencyId() {
        return emergencyId;
    }

    public void setEmergencyId(Long emergencyId) {
        this.emergencyId = emergencyId;
    }

    public String getEmergencyType() {
        return emergencyType;
    }

    public void setEmergencyType(String emergencyType) {
        this.emergencyType = emergencyType;
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

    public Long getAmbulanceId() {
        return ambulanceId;
    }

    public void setAmbulanceId(Long ambulanceId) {
        this.ambulanceId = ambulanceId;
    }

    public String getAmbulanceCallSign() {
        return ambulanceCallSign;
    }

    public void setAmbulanceCallSign(String ambulanceCallSign) {
        this.ambulanceCallSign = ambulanceCallSign;
    }

    public String getAmbulanceType() {
        return ambulanceType;
    }

    public void setAmbulanceType(String ambulanceType) {
        this.ambulanceType = ambulanceType;
    }

    public Long getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public void setTotalDistanceKm(double totalDistanceKm) {
        this.totalDistanceKm = totalDistanceKm;
    }

    public double getTotalEstimatedMinutes() {
        return totalEstimatedMinutes;
    }

    public void setTotalEstimatedMinutes(double totalEstimatedMinutes) {
        this.totalEstimatedMinutes = totalEstimatedMinutes;
    }

    public List<Long> getRouteNodeIds() {
        return routeNodeIds;
    }

    public void setRouteNodeIds(List<Long> routeNodeIds) {
        this.routeNodeIds = routeNodeIds;
    }

    public boolean isFallbackFacilityApplied() {
        return fallbackFacilityApplied;
    }

    public void setFallbackFacilityApplied(boolean fallbackFacilityApplied) {
        this.fallbackFacilityApplied = fallbackFacilityApplied;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}
