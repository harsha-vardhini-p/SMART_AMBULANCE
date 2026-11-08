package com.lifegrid.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable value object encapsulating the computed routing path,
 * cumulative distance, and estimated travel time.
 * <p>
 * Contains no framework dependencies.
 */
public final class RouteResult {

    private final double distanceKm;
    private final double estimatedMinutes;
    private final List<Long> pathNodeIds;

    public RouteResult(double distanceKm, double estimatedMinutes, List<Long> pathNodeIds) {
        this.distanceKm = Math.round(distanceKm * 100.0) / 100.0;
        this.estimatedMinutes = Math.round(estimatedMinutes * 100.0) / 100.0;
        this.pathNodeIds = pathNodeIds != null
                ? Collections.unmodifiableList(new ArrayList<>(pathNodeIds))
                : Collections.emptyList();
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public List<Long> getPathNodeIds() {
        return pathNodeIds;
    }

    public int getHopCount() {
        return Math.max(0, pathNodeIds.size() - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouteResult that = (RouteResult) o;
        return Double.compare(that.distanceKm, distanceKm) == 0 &&
                Double.compare(that.estimatedMinutes, estimatedMinutes) == 0 &&
                Objects.equals(pathNodeIds, that.pathNodeIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(distanceKm, estimatedMinutes, pathNodeIds);
    }

    @Override
    public String toString() {
        return "RouteResult{" +
                "distanceKm=" + distanceKm +
                ", estimatedMinutes=" + estimatedMinutes +
                ", path=" + pathNodeIds +
                '}';
    }
}
