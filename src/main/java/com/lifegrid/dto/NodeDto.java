package com.lifegrid.dto;

import com.lifegrid.domain.Node;

public class NodeDto {

    private Long id;
    private String name;
    private double x;
    private double y;
    private double latitude;
    private double longitude;

    public NodeDto() {
    }

    public NodeDto(Long id, String name, double x, double y, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static NodeDto fromEntity(Node node) {
        if (node == null) return null;
        return new NodeDto(
                node.getId(),
                node.getName(),
                node.getX(),
                node.getY(),
                node.getLatitude(),
                node.getLongitude()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
