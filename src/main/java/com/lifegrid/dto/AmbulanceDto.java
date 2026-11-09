package com.lifegrid.dto;

import com.lifegrid.domain.Ambulance;
import com.lifegrid.domain.AmbulanceStatus;
import com.lifegrid.domain.AmbulanceType;

public class AmbulanceDto {

    private Long id;
    private String callSign;
    private AmbulanceType type;
    private Long currentNodeId;
    private AmbulanceStatus status;

    public AmbulanceDto() {
    }

    public AmbulanceDto(Long id, String callSign, AmbulanceType type, Long currentNodeId, AmbulanceStatus status) {
        this.id = id;
        this.callSign = callSign;
        this.type = type;
        this.currentNodeId = currentNodeId;
        this.status = status;
    }

    public static AmbulanceDto fromEntity(Ambulance ambulance) {
        if (ambulance == null) return null;
        return new AmbulanceDto(
                ambulance.getId(),
                ambulance.getCallSign(),
                ambulance.getType(),
                ambulance.getCurrentNodeId(),
                ambulance.getStatus()
        );
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
}
