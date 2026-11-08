package com.lifegrid.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an incident or emergency call request within the system.
 */
@Entity
@Table(name = "emergencies")
public class Emergency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String type;

    @Column(nullable = false)
    private int severity; // 1 (minor) to 10 (critical)

    @Column(name = "location_node_id", nullable = false)
    private Long locationNodeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmergencyStatus status = EmergencyStatus.PENDING;

    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "ambulance_id")
    private Long ambulanceId;

    @Column(name = "hospital_id")
    private Long hospitalId;

    @Column(name = "estimated_minutes")
    private Double estimatedMinutes;

    public Emergency() {
        this.reportedAt = LocalDateTime.now();
    }

    public Emergency(String type, int severity, Long locationNodeId) {
        this.type = type;
        this.severity = severity;
        this.locationNodeId = locationNodeId;
        this.status = EmergencyStatus.PENDING;
        this.reportedAt = LocalDateTime.now();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Emergency emergency = (Emergency) o;
        return Objects.equals(id, emergency.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Emergency{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", severity=" + severity +
                ", locationNodeId=" + locationNodeId +
                ", status=" + status +
                '}';
    }
}
