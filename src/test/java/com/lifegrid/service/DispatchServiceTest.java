package com.lifegrid.service;

import com.lifegrid.domain.Ambulance;
import com.lifegrid.domain.AmbulanceStatus;
import com.lifegrid.domain.DispatchLog;
import com.lifegrid.domain.Emergency;
import com.lifegrid.domain.EmergencyStatus;
import com.lifegrid.domain.Hospital;
import com.lifegrid.dto.DispatchResponseDto;
import com.lifegrid.repository.AmbulanceRepository;
import com.lifegrid.repository.DispatchLogRepository;
import com.lifegrid.repository.EmergencyRepository;
import com.lifegrid.repository.HospitalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class DispatchServiceTest {

    @Autowired
    private DispatchService dispatchService;

    @Autowired
    private EmergencyRepository emergencyRepository;

    @Autowired
    private AmbulanceRepository ambulanceRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DispatchLogRepository dispatchLogRepository;

    @Autowired
    private com.lifegrid.repository.NodeRepository nodeRepository;

    @Autowired
    private NetworkService networkService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        networkService.resetSimulation();
    }

    @Test
    @DisplayName("dispatchEmergency successfully allocates resources and persists state transitions")
    void testDispatchEmergencySuccess() {
        Long node4 = nodeRepository.findAll().get(3).getId();
        // Create emergency at Besant Road (Node 4)
        Emergency emergency = new Emergency("Severe Chest Pain", 9, node4);
        emergency = emergencyRepository.save(emergency);

        Hospital initialHospital = hospitalRepository.findAll().stream()
                .filter(h -> h.hasFacility("CARDIAC") && h.getAvailableBeds() > 0)
                .findFirst()
                .orElseThrow();
        int initialBeds = initialHospital.getAvailableBeds();

        DispatchResponseDto response = dispatchService.dispatchEmergency(emergency.getId());

        assertNotNull(response);
        assertEquals(emergency.getId(), response.getEmergencyId());
        assertNotNull(response.getAmbulanceCallSign());
        assertEquals("ADVANCED", response.getAmbulanceType(), "Severity 9 must assign ADVANCED ambulance");
        assertTrue(response.getTotalEstimatedMinutes() > 0);
        assertFalse(response.getRouteNodeIds().isEmpty());

        // Verify Emergency updated in DB
        Emergency updated = emergencyRepository.findById(emergency.getId()).orElseThrow();
        assertEquals(EmergencyStatus.ASSIGNED, updated.getStatus());
        assertNotNull(updated.getAmbulanceId());
        assertNotNull(updated.getHospitalId());
        assertNotNull(updated.getAssignedAt());

        // Verify Ambulance is DISPATCHED
        Ambulance amb = ambulanceRepository.findById(updated.getAmbulanceId()).orElseThrow();
        assertEquals(AmbulanceStatus.DISPATCHED, amb.getStatus());

        // Verify Hospital bed count decremented
        Hospital targetHosp = hospitalRepository.findById(updated.getHospitalId()).orElseThrow();
        if (targetHosp.getId().equals(initialHospital.getId())) {
            assertEquals(initialBeds - 1, targetHosp.getAvailableBeds());
        }

        // Verify DispatchLog entry persisted
        List<DispatchLog> logs = dispatchLogRepository.findByEmergencyId(emergency.getId());
        assertEquals(1, logs.size());
        assertEquals(emergency.getId(), logs.get(0).getEmergencyId());
        assertEquals(amb.getId(), logs.get(0).getAmbulanceId());
    }

    @Test
    @DisplayName("dispatchNextPendingEmergency prioritizes critical severity first")
    void testDispatchNextPendingEmergencyPriority() {
        // Clean any existing pending emergencies
        emergencyRepository.deleteAll();

        List<com.lifegrid.domain.Node> nodes = nodeRepository.findAll();
        Long node1 = nodes.get(0).getId();
        Long node5 = nodes.get(4).getId();

        Emergency lowPriority = new Emergency("Minor Sprain", 2, node1);
        lowPriority.setReportedAt(LocalDateTime.now().minusMinutes(20));
        emergencyRepository.save(lowPriority);

        Emergency highPriority = new Emergency("Severe Trauma Accident", 10, node5);
        highPriority.setReportedAt(LocalDateTime.now().minusMinutes(5));
        emergencyRepository.save(highPriority);

        Optional<DispatchResponseDto> result = dispatchService.dispatchNextPendingEmergency();

        assertTrue(result.isPresent());
        assertEquals(highPriority.getId(), result.get().getEmergencyId(),
                "Critical severity 10 must be dispatched before severity 2 regardless of report time");
    }

    @Test
    @DisplayName("completeEmergency transitions status to COMPLETED and frees ambulance at hospital")
    void testCompleteEmergency() {
        Long node2 = nodeRepository.findAll().get(1).getId();
        Emergency emergency = new Emergency("Respiratory Distress", 7, node2);
        emergency = emergencyRepository.save(emergency);

        DispatchResponseDto dispatchResponse = dispatchService.dispatchEmergency(emergency.getId());
        Long ambulanceId = dispatchResponse.getAmbulanceId();
        Long hospitalId = dispatchResponse.getHospitalId();

        // Complete emergency
        dispatchService.completeEmergency(emergency.getId());

        Emergency completed = emergencyRepository.findById(emergency.getId()).orElseThrow();
        assertEquals(EmergencyStatus.COMPLETED, completed.getStatus());
        assertNotNull(completed.getCompletedAt());

        Ambulance freedAmbulance = ambulanceRepository.findById(ambulanceId).orElseThrow();
        assertEquals(AmbulanceStatus.AVAILABLE, freedAmbulance.getStatus());

        Hospital hosp = hospitalRepository.findById(hospitalId).orElseThrow();
        assertEquals(hosp.getNodeId(), freedAmbulance.getCurrentNodeId(),
                "Ambulance should now be stationed at the receiving hospital node");
    }
}
