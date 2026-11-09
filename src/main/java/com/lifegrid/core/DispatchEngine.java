package com.lifegrid.core;

import com.lifegrid.domain.Ambulance;
import com.lifegrid.domain.AmbulanceStatus;
import com.lifegrid.domain.AmbulanceType;
import com.lifegrid.domain.Emergency;
import com.lifegrid.domain.Hospital;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;

/**
 * Pure Java dispatch decision engine.
 * <p>
 * Contains core triage prioritization, nearest ambulance allocation with capability constraints,
 * and intelligent hospital selection balancing travel times, facilities, and bed scarcity penalties.
 * <p>
 * Completely free of Spring dependencies for comprehensive unit testing.
 */
public class DispatchEngine {

    /**
     * Triage priority comparator:
     * 1. Severity descending (10 down to 1)
     * 2. Reported timestamp ascending (FIFO order for identical severity)
     */
    public static final Comparator<Emergency> EMERGENCY_PRIORITY_COMPARATOR = (e1, e2) -> {
        if (e1 == null && e2 == null) return 0;
        if (e1 == null) return 1;
        if (e2 == null) return -1;

        int severityCmp = Integer.compare(e2.getSeverity(), e1.getSeverity());
        if (severityCmp != 0) {
            return severityCmp;
        }

        if (e1.getReportedAt() == null && e2.getReportedAt() == null) return 0;
        if (e1.getReportedAt() == null) return 1;
        if (e2.getReportedAt() == null) return -1;
        return e1.getReportedAt().compareTo(e2.getReportedAt());
    };

    public static final int CRITICAL_SEVERITY_THRESHOLD = 8;

    /**
     * Helper holder for selected ambulance and its route to scene.
     */
    public static class AmbulanceAssignment {
        private final Ambulance ambulance;
        private final RouteResult routeToScene;

        public AmbulanceAssignment(Ambulance ambulance, RouteResult routeToScene) {
            this.ambulance = ambulance;
            this.routeToScene = routeToScene;
        }

        public Ambulance getAmbulance() {
            return ambulance;
        }

        public RouteResult getRouteToScene() {
            return routeToScene;
        }
    }

    /**
     * Helper holder for selected receiving hospital and its route from scene.
     */
    public static class HospitalAssignment {
        private final Hospital hospital;
        private final RouteResult routeToHospital;
        private final double score;
        private final boolean fallbackApplied;

        public HospitalAssignment(Hospital hospital, RouteResult routeToHospital, double score, boolean fallbackApplied) {
            this.hospital = hospital;
            this.routeToHospital = routeToHospital;
            this.score = score;
            this.fallbackApplied = fallbackApplied;
        }

        public Hospital getHospital() {
            return hospital;
        }

        public RouteResult getRouteToHospital() {
            return routeToHospital;
        }

        public double getScore() {
            return score;
        }

        public boolean isFallbackApplied() {
            return fallbackApplied;
        }
    }

    /**
     * Creates an empty PriorityQueue initialized with LifeGrid triage comparator.
     */
    public PriorityQueue<Emergency> createEmergencyQueue() {
        return new PriorityQueue<>(EMERGENCY_PRIORITY_COMPARATOR);
    }

    /**
     * Creates a PriorityQueue containing the given collection of pending emergencies.
     */
    public PriorityQueue<Emergency> createEmergencyQueue(Collection<Emergency> emergencies) {
        PriorityQueue<Emergency> queue = new PriorityQueue<>(EMERGENCY_PRIORITY_COMPARATOR);
        if (emergencies != null) {
            for (Emergency e : emergencies) {
                if (e != null) {
                    queue.offer(e);
                }
            }
        }
        return queue;
    }

    /**
     * Resolves the primary clinical facility required based on emergency incident type.
     */
    public String resolveRequiredFacility(String emergencyType) {
        if (emergencyType == null || emergencyType.trim().isEmpty()) {
            return "GENERAL";
        }
        String upper = emergencyType.toUpperCase();
        if (upper.contains("CARDIAC") || upper.contains("HEART") || upper.contains("CHEST")) {
            return "CARDIAC";
        }
        if (upper.contains("TRAUMA") || upper.contains("ACCIDENT") || upper.contains("CRASH") || upper.contains("FALL")) {
            return "TRAUMA";
        }
        if (upper.contains("PEDIATRIC") || upper.contains("CHILD") || upper.contains("INFANT")) {
            return "PEDIATRIC";
        }
        if (upper.contains("ICU") || upper.contains("RESPIRATORY") || upper.contains("STROKE") || upper.contains("CRITICAL")) {
            return "ICU";
        }
        return "GENERAL";
    }

    /**
     * Allocates the nearest AVAILABLE ambulance to the incident scene.
     * Enforces ADVANCED ambulance requirement for severity &gt;= 8.
     *
     * @param emergency           the emergency ticket
     * @param candidateAmbulances all ambulance records in system
     * @param graph               road network graph
     * @param router              Dijkstra router
     * @param trafficMultiplier   congestion coefficient
     * @return {@link AmbulanceAssignment} with chosen vehicle and route
     * @throws NoAmbulanceAvailableException if no suitable or reachable ambulance is found
     */
    public AmbulanceAssignment selectAmbulance(Emergency emergency,
                                              List<Ambulance> candidateAmbulances,
                                              Graph graph,
                                              DijkstraRouter router,
                                              double trafficMultiplier) {
        if (emergency == null) {
            throw new IllegalArgumentException("Emergency ticket cannot be null");
        }
        if (candidateAmbulances == null || candidateAmbulances.isEmpty()) {
            throw new NoAmbulanceAvailableException("No ambulances exist in the system fleet");
        }

        boolean requiresAdvanced = emergency.getSeverity() >= CRITICAL_SEVERITY_THRESHOLD;

        // 1. Filter available units with capability match
        List<Ambulance> eligible = new ArrayList<>();
        for (Ambulance amb : candidateAmbulances) {
            if (amb.getStatus() == AmbulanceStatus.AVAILABLE) {
                if (!requiresAdvanced || amb.getType() == AmbulanceType.ADVANCED) {
                    eligible.add(amb);
                }
            }
        }

        if (eligible.isEmpty()) {
            if (requiresAdvanced) {
                throw new NoAmbulanceAvailableException(
                        "No AVAILABLE ADVANCED ambulance available for critical emergency (Severity: "
                                + emergency.getSeverity() + ", Type: " + emergency.getType() + ")"
                );
            } else {
                throw new NoAmbulanceAvailableException(
                        "All ambulance units are currently busy or unavailable in the fleet"
                );
            }
        }

        // 2. Select closest eligible ambulance by Dijkstra travel time
        Ambulance bestAmbulance = null;
        RouteResult bestRoute = null;

        for (Ambulance amb : eligible) {
            try {
                RouteResult route = router.findShortestPath(
                        graph,
                        amb.getCurrentNodeId(),
                        emergency.getLocationNodeId(),
                        trafficMultiplier
                );

                if (bestRoute == null || route.getEstimatedMinutes() < bestRoute.getEstimatedMinutes()) {
                    bestRoute = route;
                    bestAmbulance = amb;
                }
            } catch (RoutingException ex) {
                // Skip if this particular ambulance is in an isolated or unreachable node
            }
        }

        if (bestAmbulance == null || bestRoute == null) {
            throw new NoAmbulanceAvailableException(
                    "No available eligible ambulance has a navigable route to emergency location node "
                            + emergency.getLocationNodeId()
            );
        }

        return new AmbulanceAssignment(bestAmbulance, bestRoute);
    }

    /**
     * Selects the optimal receiving hospital from the emergency scene.
     * Evaluates hospitals having available beds &gt; 0, prioritizing matching medical facilities,
     * with fallback to next best hospital, ranked by {@link DispatchScoringConfig}.
     *
     * @param emergency         the emergency ticket
     * @param candidateHospitals hospital pool
     * @param graph             road network graph
     * @param router            Dijkstra router
     * @param scoringConfig     scoring weights for travel time vs bed scarcity
     * @param trafficMultiplier congestion coefficient
     * @return {@link HospitalAssignment} containing chosen hospital, route, and suitability score
     * @throws HospitalFullException if all hospitals are at zero bed capacity or unreachable
     */
    public HospitalAssignment selectHospital(Emergency emergency,
                                             List<Hospital> candidateHospitals,
                                             Graph graph,
                                             DijkstraRouter router,
                                             DispatchScoringConfig scoringConfig,
                                             double trafficMultiplier) {
        if (emergency == null) {
            throw new IllegalArgumentException("Emergency ticket cannot be null");
        }
        if (candidateHospitals == null || candidateHospitals.isEmpty()) {
            throw new HospitalFullException("No hospitals registered in the LifeGrid network");
        }

        // Global bed check
        boolean anyBedAvailable = candidateHospitals.stream().anyMatch(h -> h.getAvailableBeds() > 0);
        if (!anyBedAvailable) {
            throw new HospitalFullException(
                    "All hospitals in the regional network are currently at 100% capacity (0 beds available)"
            );
        }

        String requiredFacility = resolveRequiredFacility(emergency.getType());

        // Phase 1: Try hospitals that have available beds AND the specific clinical facility
        List<Hospital> matchingHospitals = new ArrayList<>();
        for (Hospital h : candidateHospitals) {
            if (h.getAvailableBeds() > 0 && h.hasFacility(requiredFacility)) {
                matchingHospitals.add(h);
            }
        }

        HospitalAssignment assignment = evaluateHospitals(
                emergency.getLocationNodeId(),
                matchingHospitals,
                graph,
                router,
                scoringConfig,
                trafficMultiplier,
                false
        );

        if (assignment != null) {
            return assignment;
        }

        // Phase 2: Fallback to next best hospital with available beds (facility fallback)
        List<Hospital> fallbackHospitals = new ArrayList<>();
        for (Hospital h : candidateHospitals) {
            if (h.getAvailableBeds() > 0) {
                fallbackHospitals.add(h);
            }
        }

        assignment = evaluateHospitals(
                emergency.getLocationNodeId(),
                fallbackHospitals,
                graph,
                router,
                scoringConfig,
                trafficMultiplier,
                true
        );

        if (assignment == null) {
            throw new HospitalFullException(
                    "No hospital with available beds has a navigable road path from emergency location node "
                            + emergency.getLocationNodeId()
            );
        }

        return assignment;
    }

    private HospitalAssignment evaluateHospitals(Long sceneNodeId,
                                                 List<Hospital> hospitals,
                                                 Graph graph,
                                                 DijkstraRouter router,
                                                 DispatchScoringConfig scoringConfig,
                                                 double trafficMultiplier,
                                                 boolean isFallback) {
        Hospital bestHospital = null;
        RouteResult bestRoute = null;
        double bestScore = Double.POSITIVE_INFINITY;

        for (Hospital hospital : hospitals) {
            try {
                RouteResult route = router.findShortestPath(
                        graph,
                        sceneNodeId,
                        hospital.getNodeId(),
                        trafficMultiplier
                );

                double score = scoringConfig.calculateScore(
                        route.getEstimatedMinutes(),
                        hospital.getAvailableBeds(),
                        hospital.getTotalBeds()
                );

                if (score < bestScore) {
                    bestScore = score;
                    bestHospital = hospital;
                    bestRoute = route;
                }
            } catch (RoutingException ex) {
                // Node unreachable, skip candidate
            }
        }

        if (bestHospital == null || bestRoute == null) {
            return null;
        }

        return new HospitalAssignment(bestHospital, bestRoute, bestScore, isFallback);
    }

    /**
     * Executes complete dispatch allocation matching emergency to nearest suitable ambulance
     * and destination hospital.
     */
    public DispatchDecision dispatch(Emergency emergency,
                                     List<Ambulance> candidateAmbulances,
                                     List<Hospital> candidateHospitals,
                                     Graph graph,
                                     DijkstraRouter router,
                                     DispatchScoringConfig scoringConfig,
                                     double trafficMultiplier) {
        AmbulanceAssignment ambulanceAssignment = selectAmbulance(
                emergency, candidateAmbulances, graph, router, trafficMultiplier
        );

        HospitalAssignment hospitalAssignment = selectHospital(
                emergency, candidateHospitals, graph, router, scoringConfig, trafficMultiplier
        );

        return new DispatchDecision(
                emergency,
                ambulanceAssignment.getAmbulance(),
                hospitalAssignment.getHospital(),
                ambulanceAssignment.getRouteToScene(),
                hospitalAssignment.getRouteToHospital(),
                hospitalAssignment.getScore(),
                hospitalAssignment.isFallbackApplied()
        );
    }
}
