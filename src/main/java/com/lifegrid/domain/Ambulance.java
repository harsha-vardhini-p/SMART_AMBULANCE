package com.lifegrid.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Represents an emergency response ambulance vehicle stationed or moving in the network.
 */
@Entity
@Table(name = "ambulances")
public class Ambulance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_sign", nullable = false, unique = true, length = 50)
    private String callSign;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AmbulanceType type;

    @Column(name = "current_node_id", nullable = false)
    private Long currentNodeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AmbulanceStatus status = AmbulanceStatus.AVAILABLE;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "progress")
    private Double progress = 0.0;

    public Ambulance() {
    }

    public Ambulance(String callSign, AmbulanceType type, Long currentNodeId, AmbulanceStatus status) {
        this.callSign = callSign;
        this.type = type;
        this.currentNodeId = currentNodeId;
        this.status = status;
        this.progress = 0.0;
    }

    public Ambulance(Long id, String callSign, AmbulanceType type, Long currentNodeId, AmbulanceStatus status) {
        this.id = id;
        this.callSign = callSign;
        this.type = type;
        this.currentNodeId = currentNodeId;
        this.status = status;
        this.progress = 0.0;
    }

    public Ambulance(Long id, String callSign, AmbulanceType type, Long currentNodeId, AmbulanceStatus status, Double latitude, Double longitude) {
        this.id = id;
        this.callSign = callSign;
        this.type = type;
        this.currentNodeId = currentNodeId;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.progress = 0.0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCallSign() {
        return callSign;
    }

    public void setCallSign(String callSign) {
        this.callSign = callSign;
    }

    public AmbulanceType getType() {
        return type;
    }

    public void setType(AmbulanceType type) {
        this.type = type;
    }

    public Long getCurrentNodeId() {
        return currentNodeId;
    }

    public void setCurrentNodeId(Long currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    public AmbulanceStatus getStatus() {
        return status;
    }

    public void setStatus(AmbulanceStatus status) {
        this.status = status;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getProgress() {
        return progress != null ? progress : 0.0;
    }

    public void setProgress(Double progress) {
        this.progress = progress != null ? Math.round(progress * 100.0) / 100.0 : 0.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ambulance ambulance = (Ambulance) o;
        return Objects.equals(id, ambulance.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Ambulance{" +
                "id=" + id +
                ", callSign='" + callSign + '\'' +
                ", type=" + type +
                ", currentNodeId=" + currentNodeId +
                ", status=" + status +
                ", lat=" + latitude +
                ", lon=" + longitude +
                ", progress=" + progress + "%" +
                '}';
    }
}
