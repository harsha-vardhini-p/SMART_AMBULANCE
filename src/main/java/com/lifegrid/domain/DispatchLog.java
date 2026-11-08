package com.lifegrid.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Historical dispatch audit record capturing routing and assignment decisions.
 */
@Entity
@Table(name = "dispatch_logs")
public class DispatchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "emergency_id", nullable = false)
    private Long emergencyId;

    @Column(name = "ambulance_id", nullable = false)
    private Long ambulanceId;

    @Column(name = "hospital_id", nullable = false)
    private Long hospitalId;

    @Column(name = "route_nodes", columnDefinition = "TEXT", nullable = false)
    private String routeNodes; // JSON array or comma-separated node path [id1, id2, ...]

    @Column(name = "estimated_minutes", nullable = false)
    private double estimatedMinutes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public DispatchLog() {
        this.createdAt = LocalDateTime.now();
    }

    public DispatchLog(Long emergencyId, Long ambulanceId, Long hospitalId, String routeNodes, double estimatedMinutes) {
        this.emergencyId = emergencyId;
        this.ambulanceId = ambulanceId;
        this.hospitalId = hospitalId;
        this.routeNodes = routeNodes;
        this.estimatedMinutes = estimatedMinutes;
        this.createdAt = LocalDateTime.now();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DispatchLog that = (DispatchLog) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DispatchLog{" +
                "id=" + id +
                ", emergencyId=" + emergencyId +
                ", ambulanceId=" + ambulanceId +
                ", hospitalId=" + hospitalId +
                ", estimatedMinutes=" + estimatedMinutes +
                '}';
    }
}
