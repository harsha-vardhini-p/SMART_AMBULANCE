package com.lifegrid.dto;

public class HospitalUtilisationDto {

    private Long hospitalId;
    private String hospitalName;
    private int totalBeds;
    private int availableBeds;
    private int occupiedBeds;
    private double utilisationPercentage;

    public HospitalUtilisationDto() {
    }

    public HospitalUtilisationDto(Long hospitalId, String hospitalName, int totalBeds, int availableBeds) {
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
        this.totalBeds = totalBeds;
        this.availableBeds = availableBeds;
        this.occupiedBeds = Math.max(0, totalBeds - availableBeds);
        this.utilisationPercentage = totalBeds > 0
                ? Math.round(((double) occupiedBeds / totalBeds) * 10000.0) / 100.0
                : 0.0;
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

    public int getOccupiedBeds() {
        return occupiedBeds;
    }

    public void setOccupiedBeds(int occupiedBeds) {
        this.occupiedBeds = occupiedBeds;
    }

    public double getUtilisationPercentage() {
        return utilisationPercentage;
    }

    public void setUtilisationPercentage(double utilisationPercentage) {
        this.utilisationPercentage = utilisationPercentage;
    }
}
