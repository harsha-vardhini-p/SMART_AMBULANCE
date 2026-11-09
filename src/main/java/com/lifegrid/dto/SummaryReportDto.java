package com.lifegrid.dto;

import java.util.List;

public class SummaryReportDto {

    private long totalDispatches;
    private double averageResponseMinutes;
    private long unassignedCount;
    private long totalAmbulances;
    private long availableAmbulances;
    private long dispatchedAmbulances;
    private List<HospitalUtilisationDto> hospitalUtilisation;

    public SummaryReportDto() {
    }

    public SummaryReportDto(long totalDispatches,
                            double averageResponseMinutes,
                            long unassignedCount,
                            long totalAmbulances,
                            long availableAmbulances,
                            long dispatchedAmbulances,
                            List<HospitalUtilisationDto> hospitalUtilisation) {
        this.totalDispatches = totalDispatches;
        this.averageResponseMinutes = Math.round(averageResponseMinutes * 100.0) / 100.0;
        this.unassignedCount = unassignedCount;
        this.totalAmbulances = totalAmbulances;
        this.availableAmbulances = availableAmbulances;
        this.dispatchedAmbulances = dispatchedAmbulances;
        this.hospitalUtilisation = hospitalUtilisation;
    }

    public long getTotalDispatches() {
        return totalDispatches;
    }

    public void setTotalDispatches(long totalDispatches) {
        this.totalDispatches = totalDispatches;
    }

    public double getAverageResponseMinutes() {
        return averageResponseMinutes;
    }

    public void setAverageResponseMinutes(double averageResponseMinutes) {
        this.averageResponseMinutes = averageResponseMinutes;
    }

    public long getUnassignedCount() {
        return unassignedCount;
    }

    public void setUnassignedCount(long unassignedCount) {
        this.unassignedCount = unassignedCount;
    }

    public long getTotalAmbulances() {
        return totalAmbulances;
    }

    public void setTotalAmbulances(long totalAmbulances) {
        this.totalAmbulances = totalAmbulances;
    }

    public long getAvailableAmbulances() {
        return availableAmbulances;
    }

    public void setAvailableAmbulances(long availableAmbulances) {
        this.availableAmbulances = availableAmbulances;
    }

    public long getDispatchedAmbulances() {
        return dispatchedAmbulances;
    }

    public void setDispatchedAmbulances(long dispatchedAmbulances) {
        this.dispatchedAmbulances = dispatchedAmbulances;
    }

    public List<HospitalUtilisationDto> getHospitalUtilisation() {
        return hospitalUtilisation;
    }

    public void setHospitalUtilisation(List<HospitalUtilisationDto> hospitalUtilisation) {
        this.hospitalUtilisation = hospitalUtilisation;
    }
}
