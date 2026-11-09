package com.lifegrid.dto;

import com.lifegrid.domain.Hospital;

import java.util.HashSet;
import java.util.Set;

public class HospitalDto {

    private Long id;
    private String name;
    private Long nodeId;
    private int totalBeds;
    private int availableBeds;
    private Set<String> facilities;

    public HospitalDto() {
    }

    public HospitalDto(Long id, String name, Long nodeId, int totalBeds, int availableBeds, Set<String> facilities) {
        this.id = id;
        this.name = name;
        this.nodeId = nodeId;
        this.totalBeds = totalBeds;
        this.availableBeds = availableBeds;
        this.facilities = facilities != null ? new HashSet<>(facilities) : new HashSet<>();
    }

    public static HospitalDto fromEntity(Hospital hospital) {
        if (hospital == null) return null;
        return new HospitalDto(
                hospital.getId(),
                hospital.getName(),
                hospital.getNodeId(),
                hospital.getTotalBeds(),
                hospital.getAvailableBeds(),
                hospital.getFacilities()
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

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public int getTotalBeds() {
        return totalBeds;
    }

    public void setTotalBeds(int totalBeds) {
        this.totalBeds = totalBeds;
    }

    public int getAvailableBeds() {
        return availableBeds;
    }

    public void setAvailableBeds(int availableBeds) {
        this.availableBeds = availableBeds;
    }

    public Set<String> getFacilities() {
        return facilities;
    }

    public void setFacilities(Set<String> facilities) {
        this.facilities = facilities;
    }
}
