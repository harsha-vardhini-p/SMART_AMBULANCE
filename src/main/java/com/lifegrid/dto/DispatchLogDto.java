package com.lifegrid.dto;

import com.lifegrid.domain.DispatchLog;

import java.time.LocalDateTime;

public class DispatchLogDto {

    private Long id;
    private Long emergencyId;
    private Long ambulanceId;
    private Long hospitalId;
    private String routeNodes;
    private double estimatedMinutes;
    private LocalDateTime createdAt;

    public DispatchLogDto() {
    }

    public DispatchLogDto(Long id, Long emergencyId, Long ambulanceId, Long hospitalId, String routeNodes, double estimatedMinutes, LocalDateTime createdAt) {
        this.id = id;
        this.emergencyId = emergencyId;
        this.ambulanceId = ambulanceId;
        this.hospitalId = hospitalId;
        this.routeNodes = routeNodes;
        this.estimatedMinutes = estimatedMinutes;
        this.createdAt = createdAt;
    }

    public static DispatchLogDto fromEntity(DispatchLog log) {
        if (log == null) return null;
        return new DispatchLogDto(
                log.getId(),
                log.getEmergencyId(),
                log.getAmbulanceId(),
                log.getHospitalId(),
                log.getRouteNodes(),
                log.getEstimatedMinutes(),
                log.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmergencyId() {
        return emergencyId;
    }

    public void setEmergencyId(Long emergencyId) {
        this.emergencyId = emergencyId;
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

    public String getRouteNodes() {
        return routeNodes;
    }

    public void setRouteNodes(String routeNodes) {
        this.routeNodes = routeNodes;
    }

    public double getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(double estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
