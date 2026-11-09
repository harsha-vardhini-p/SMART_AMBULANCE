package com.lifegrid.dto;

import com.lifegrid.domain.Road;

public class RoadDto {

    private Long id;
    private Long fromNodeId;
    private String fromNodeName;
    private Long toNodeId;
    private String toNodeName;
    private double distanceKm;
    private boolean bidirectional;

    public RoadDto() {
    }

    public RoadDto(Long id, Long fromNodeId, String fromNodeName, Long toNodeId, String toNodeName, double distanceKm, boolean bidirectional) {
        this.id = id;
        this.fromNodeId = fromNodeId;
        this.fromNodeName = fromNodeName;
        this.toNodeId = toNodeId;
        this.toNodeName = toNodeName;
        this.distanceKm = distanceKm;
        this.bidirectional = bidirectional;
    }

    public static RoadDto fromEntity(Road road) {
        if (road == null) return null;
        return new RoadDto(
                road.getId(),
                road.getFromNode() != null ? road.getFromNode().getId() : null,
                road.getFromNode() != null ? road.getFromNode().getName() : null,
                road.getToNode() != null ? road.getToNode().getId() : null,
                road.getToNode() != null ? road.getToNode().getName() : null,
                road.getDistanceKm(),
                road.isBidirectional()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFromNodeId() {
        return fromNodeId;
    }

    public void setFromNodeId(Long fromNodeId) {
        this.fromNodeId = fromNodeId;
    }

    public String getFromNodeName() {
        return fromNodeName;
    }

    public void setFromNodeName(String fromNodeName) {
        this.fromNodeName = fromNodeName;
    }

    public Long getToNodeId() {
        return toNodeId;
    }

    public void setToNodeId(Long toNodeId) {
        this.toNodeId = toNodeId;
    }

    public String getToNodeName() {
        return toNodeName;
    }

    public void setToNodeName(String toNodeName) {
        this.toNodeName = toNodeName;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public boolean isBidirectional() {
        return bidirectional;
    }

    public void setBidirectional(boolean bidirectional) {
        this.bidirectional = bidirectional;
    }
}
