package com.lifegrid.core;

import com.lifegrid.domain.Ambulance;
import com.lifegrid.domain.Emergency;
import com.lifegrid.domain.Hospital;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Value object representing a complete end-to-end dispatch decision:
 * emergency ticket, allocated ambulance, target receiving hospital, and routes.
 */
public final class DispatchDecision {

    private final Emergency emergency;
    private final Ambulance ambulance;
    private final Hospital hospital;
    private final RouteResult routeToScene;
    private final RouteResult routeToHospital;
    private final double totalDistanceKm;
    private final double totalEstimatedMinutes;
    private final List<Long> combinedRouteNodeIds;
    private final double hospitalScore;
    private final boolean fallbackFacilityApplied;

    public DispatchDecision(Emergency emergency,
                            Ambulance ambulance,
                            Hospital hospital,
                            RouteResult routeToScene,
                            RouteResult routeToHospital,
                            double hospitalScore,
                            boolean fallbackFacilityApplied) {
        this.emergency = emergency;
        this.ambulance = ambulance;
        this.hospital = hospital;
        this.routeToScene = routeToScene;
        this.routeToHospital = routeToHospital;
        this.hospitalScore = Math.round(hospitalScore * 100.0) / 100.0;
        this.fallbackFacilityApplied = fallbackFacilityApplied;

        double dScene = routeToScene != null ? routeToScene.getDistanceKm() : 0.0;
        double dHosp = routeToHospital != null ? routeToHospital.getDistanceKm() : 0.0;
        this.totalDistanceKm = Math.round((dScene + dHosp) * 100.0) / 100.0;

        double tScene = routeToScene != null ? routeToScene.getEstimatedMinutes() : 0.0;
        double tHosp = routeToHospital != null ? routeToHospital.getEstimatedMinutes() : 0.0;
        this.totalEstimatedMinutes = Math.round((tScene + tHosp) * 100.0) / 100.0;

        // Combine paths avoiding duplicate intersection at scene
        List<Long> fullPath = new ArrayList<>();
        if (routeToScene != null) {
            fullPath.addAll(routeToScene.getPathNodeIds());
        }
        if (routeToHospital != null) {
            List<Long> hospNodes = routeToHospital.getPathNodeIds();
            if (!fullPath.isEmpty() && !hospNodes.isEmpty() &&
                    fullPath.get(fullPath.size() - 1).equals(hospNodes.get(0))) {
                fullPath.addAll(hospNodes.subList(1, hospNodes.size()));
            } else {
                fullPath.addAll(hospNodes);
            }
        }
        this.combinedRouteNodeIds = Collections.unmodifiableList(fullPath);
    }

    public Emergency getEmergency() {
        return emergency;
    }

    public Ambulance getAmbulance() {
        return ambulance;
    }

    public Hospital getHospital() {
        return hospital;
    }

    public RouteResult getRouteToScene() {
        return routeToScene;
    }

    public RouteResult getRouteToHospital() {
        return routeToHospital;
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public double getTotalEstimatedMinutes() {
        return totalEstimatedMinutes;
    }

    public List<Long> getCombinedRouteNodeIds() {
        return combinedRouteNodeIds;
    }

    public double getHospitalScore() {
        return hospitalScore;
    }

    public boolean isFallbackFacilityApplied() {
        return fallbackFacilityApplied;
    }

    @Override
    public String toString() {
        return "DispatchDecision{" +
                "emergencyId=" + (emergency != null ? emergency.getId() : null) +
                ", ambulance=" + (ambulance != null ? ambulance.getCallSign() : null) +
                ", hospital=" + (hospital != null ? hospital.getName() : null) +
                ", totalDistanceKm=" + totalDistanceKm +
                ", totalEstimatedMinutes=" + totalEstimatedMinutes +
                ", fallbackFacility=" + fallbackFacilityApplied +
                '}';
    }
}
