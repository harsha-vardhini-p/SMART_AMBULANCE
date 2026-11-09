package com.lifegrid.core;

import com.lifegrid.domain.Ambulance;
import com.lifegrid.domain.AmbulanceStatus;
import com.lifegrid.domain.AmbulanceType;
import com.lifegrid.domain.Emergency;
import com.lifegrid.domain.EmergencyStatus;
import com.lifegrid.domain.Hospital;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for pure-Java DispatchEngine triage, allocation, and hospital scoring algorithms.
 */
class DispatchEngineTest {

    private DispatchEngine engine;
    private DijkstraRouter router;
    private DispatchScoringConfig scoringConfig;
    private Graph graph;

    @BeforeEach
    void setUp() {
        engine = new DispatchEngine();
        router = new DijkstraRouter(40.0);
        scoringConfig = new DispatchScoringConfig(1.0, 4.0, 5);

        /*
         * Simple linear network:
         * (1) <--- 2.0 km ---> (2) <--- 3.0 km ---> (3) <--- 4.0 km ---> (4)
         */
        graph = new Graph();
        graph.addEdge(1L, 2L, 2.0, true);
        graph.addEdge(2L, 3L, 3.0, true);
        graph.addEdge(3L, 4L, 4.0, true);
    }

    @Test
    @DisplayName("PriorityQueue orders emergencies by severity descending, then reportedAt ascending")
    void testEmergencyPriorityOrder() {
        LocalDateTime baseTime = LocalDateTime.of(2026, 11, 9, 10, 0, 0);

        Emergency eLow = new Emergency("Fever", 3, 1L);
        eLow.setReportedAt(baseTime);

        Emergency eHighEarlier = new Emergency("Cardiac Arrest", 9, 2L);
        eHighEarlier.setReportedAt(baseTime.plusMinutes(2));

        Emergency eHighLater = new Emergency("Severe Trauma", 9, 3L);
        eHighLater.setReportedAt(baseTime.plusMinutes(5));

        Emergency eMid = new Emergency("Fracture", 6, 1L);
        eMid.setReportedAt(baseTime.minusMinutes(10));

        PriorityQueue<Emergency> pq = engine.createEmergencyQueue(Arrays.asList(eLow, eHighLater, eMid, eHighEarlier));

        // Expectation:
        // 1st: eHighEarlier (Severity 9, 10:02)
        // 2nd: eHighLater (Severity 9, 10:05)
        // 3rd: eMid (Severity 6)
        // 4th: eLow (Severity 3)
        assertEquals(eHighEarlier, pq.poll());
        assertEquals(eHighLater, pq.poll());
        assertEquals(eMid, pq.poll());
        assertEquals(eLow, pq.poll());
    }

    @Test
    @DisplayName("Ambulance selection picks nearest available unit for moderate severity")
    void testAmbulanceChoiceModerateSeverity() {
        // Emergency at node 2, severity 5 (standard)
        Emergency emergency = new Emergency("Minor Crash", 5, 2L);

        // Amb A (BASIC) at node 1 (distance to node 2: 2.0 km)
        Ambulance ambA = new Ambulance(1L, "AMB-A", AmbulanceType.BASIC, 1L, AmbulanceStatus.AVAILABLE);
        // Amb B (ADVANCED) at node 4 (distance to node 2: 7.0 km)
        Ambulance ambB = new Ambulance(2L, "AMB-B", AmbulanceType.ADVANCED, 4L, AmbulanceStatus.AVAILABLE);

        DispatchEngine.AmbulanceAssignment assignment = engine.selectAmbulance(
                emergency, List.of(ambA, ambB), graph, router, 1.0
        );

        assertNotNull(assignment);
        assertEquals(ambA, assignment.getAmbulance(), "Should select closest available ambulance");
        assertEquals(2.0, assignment.getRouteToScene().getDistanceKm(), 0.001);
    }

    @Test
    @DisplayName("Ambulance selection requires ADVANCED ambulance for severity >= 8 even if BASIC is closer")
    void testAmbulanceChoiceCriticalRequiresAdvanced() {
        // Critical emergency at node 2, severity 9
        Emergency emergency = new Emergency("Cardiac Arrest", 9, 2L);

        // Amb A (BASIC) at node 2 (0.0 km away)
        Ambulance ambA = new Ambulance(1L, "AMB-BASIC", AmbulanceType.BASIC, 2L, AmbulanceStatus.AVAILABLE);
        // Amb B (ADVANCED) at node 4 (7.0 km away)
        Ambulance ambB = new Ambulance(2L, "AMB-ADVANCED", AmbulanceType.ADVANCED, 4L, AmbulanceStatus.AVAILABLE);

        DispatchEngine.AmbulanceAssignment assignment = engine.selectAmbulance(
                emergency, List.of(ambA, ambB), graph, router, 1.0
        );

        assertNotNull(assignment);
        assertEquals(ambB, assignment.getAmbulance(), "Must enforce ADVANCED ambulance for severity >= 8");
        assertEquals(7.0, assignment.getRouteToScene().getDistanceKm(), 0.001);
    }

    @Test
    @DisplayName("Hospital selection prioritizes matching facility and bed availability")
    void testHospitalSelectionMatchingFacility() {
        // Emergency at node 2 requiring CARDIAC
        Emergency emergency = new Emergency("Cardiac Emergency", 7, 2L);

        // Hospital 1 at node 1 (2.0 km): Only GENERAL facility, 10 beds
        Hospital hGeneral = new Hospital(1L, "City General", 1L, 50, 10, Set.of("GENERAL"));

        // Hospital 2 at node 3 (3.0 km): Has CARDIAC facility, 8 beds
        Hospital hCardiac = new Hospital(2L, "Heart Institute", 3L, 50, 8, Set.of("CARDIAC", "GENERAL"));

        DispatchEngine.HospitalAssignment assignment = engine.selectHospital(
                emergency, List.of(hGeneral, hCardiac), graph, router, scoringConfig, 1.0
        );

        assertNotNull(assignment);
        assertEquals(hCardiac, assignment.getHospital(), "Should pick cardiac facility despite slightly further distance");
        assertFalse(assignment.isFallbackApplied());
    }

    @Test
    @DisplayName("Hospital selection falls back to next best hospital when specialized hospital is full")
    void testHospitalFallbackWhenSpecializedFull() {
        // Emergency at node 2 requiring CARDIAC
        Emergency emergency = new Emergency("Cardiac Emergency", 7, 2L);

        // Hospital 1 at node 1: GENERAL facility, 15 beds available
        Hospital hGeneral = new Hospital(1L, "City General", 1L, 50, 15, Set.of("GENERAL"));

        // Hospital 2 at node 3: CARDIAC facility, but 0 beds available!
        Hospital hCardiacFull = new Hospital(2L, "Heart Institute", 3L, 50, 0, Set.of("CARDIAC"));

        DispatchEngine.HospitalAssignment assignment = engine.selectHospital(
                emergency, List.of(hGeneral, hCardiacFull), graph, router, scoringConfig, 1.0
        );

        assertNotNull(assignment);
        assertEquals(hGeneral, assignment.getHospital(), "Should fallback to General Hospital when Cardiac is full");
        assertTrue(assignment.isFallbackApplied(), "Fallback flag must be true");
    }

    @Test
    @DisplayName("All ambulances busy throws NoAmbulanceAvailableException")
    void testAllAmbulancesBusy() {
        Emergency emergency = new Emergency("Accident", 6, 2L);

        Ambulance busyA = new Ambulance(1L, "AMB-1", AmbulanceType.BASIC, 1L, AmbulanceStatus.DISPATCHED);
        Ambulance busyB = new Ambulance(2L, "AMB-2", AmbulanceType.ADVANCED, 3L, AmbulanceStatus.AT_SCENE);

        assertThrows(NoAmbulanceAvailableException.class, () ->
                engine.selectAmbulance(emergency, List.of(busyA, busyB), graph, router, 1.0)
        );
    }

    @Test
    @DisplayName("Critical emergency with only BASIC ambulances available throws NoAmbulanceAvailableException")
    void testCriticalEmergencyNoAdvancedAmbulanceAvailable() {
        Emergency critical = new Emergency("Major Stroke", 9, 2L);

        // Only BASIC available
        Ambulance basicA = new Ambulance(1L, "AMB-1", AmbulanceType.BASIC, 1L, AmbulanceStatus.AVAILABLE);

        NoAmbulanceAvailableException ex = assertThrows(NoAmbulanceAvailableException.class, () ->
                engine.selectAmbulance(critical, List.of(basicA), graph, router, 1.0)
        );
        assertTrue(ex.getMessage().contains("No AVAILABLE ADVANCED ambulance available"));
    }

    @Test
    @DisplayName("All hospitals full throws HospitalFullException")
    void testAllHospitalsFull() {
        Emergency emergency = new Emergency("Trauma", 7, 2L);

        Hospital fullA = new Hospital(1L, "Hosp A", 1L, 100, 0, Set.of("TRAUMA", "GENERAL"));
        Hospital fullB = new Hospital(2L, "Hosp B", 3L, 80, 0, Set.of("TRAUMA", "CARDIAC"));

        HospitalFullException ex = assertThrows(HospitalFullException.class, () ->
                engine.selectHospital(emergency, List.of(fullA, fullB), graph, router, scoringConfig, 1.0)
        );
        assertTrue(ex.getMessage().contains("100% capacity"));
    }
}
