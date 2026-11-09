package com.lifegrid.core;

/**
 * Configuration and analytical scoring function for hospital selection.
 * <p>
 * Balances transit travel time against hospital bed congestion to prevent
 * funneling all patients into an already overwhelmed facility.
 * <p>
 * Formula:
 * <pre>
 * score = (travelMinutesWeight * travelMinutes) + (bedPenaltyWeight * bedPenalty)
 * </pre>
 * A lower score indicates a more suitable destination hospital.
 */
public class DispatchScoringConfig {

    private final double travelMinutesWeight;
    private final double bedPenaltyWeight;
    private final int lowBedThreshold;

    public DispatchScoringConfig() {
        this(1.0, 4.0, 5);
    }

    public DispatchScoringConfig(double travelMinutesWeight, double bedPenaltyWeight, int lowBedThreshold) {
        if (travelMinutesWeight <= 0) {
            throw new IllegalArgumentException("Travel minutes weight must be positive");
        }
        if (bedPenaltyWeight < 0) {
            throw new IllegalArgumentException("Bed penalty weight cannot be negative");
        }
        if (lowBedThreshold < 1) {
            throw new IllegalArgumentException("Low bed threshold must be at least 1");
        }
        this.travelMinutesWeight = travelMinutesWeight;
        this.bedPenaltyWeight = bedPenaltyWeight;
        this.lowBedThreshold = lowBedThreshold;
    }

    /**
     * Computes the dispatch suitability score for a destination hospital.
     *
     * @param travelMinutes estimated transit minutes from scene to hospital
     * @param availableBeds current number of unallocated beds
     * @param totalBeds     maximum bed capacity
     * @return calculated suitability score (lower is better)
     */
    public double calculateScore(double travelMinutes, int availableBeds, int totalBeds) {
        if (availableBeds <= 0) {
            return Double.POSITIVE_INFINITY;
        }

        double travelComponent = travelMinutesWeight * travelMinutes;

        // Occupancy fraction: 0.0 (empty) to 1.0 (full)
        double occupancyRatio = totalBeds > 0
                ? (double) (totalBeds - availableBeds) / totalBeds
                : 0.5;

        // Progressive penalty when available beds dip below threshold
        double scarcityFactor = 0.0;
        if (availableBeds <= lowBedThreshold) {
            scarcityFactor = (double) (lowBedThreshold - availableBeds + 1) / lowBedThreshold;
        }

        double bedPenalty = (occupancyRatio * 2.0) + (scarcityFactor * 5.0);
        return travelComponent + (bedPenaltyWeight * bedPenalty);
    }

    public double getTravelMinutesWeight() {
        return travelMinutesWeight;
    }

    public double getBedPenaltyWeight() {
        return bedPenaltyWeight;
    }

    public int getLowBedThreshold() {
        return lowBedThreshold;
    }
}
