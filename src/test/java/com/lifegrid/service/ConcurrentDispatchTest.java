package com.lifegrid.service;

import com.lifegrid.domain.Ambulance;
import com.lifegrid.domain.AmbulanceStatus;
import com.lifegrid.domain.Emergency;
import com.lifegrid.domain.EmergencyStatus;
import com.lifegrid.domain.Node;
import com.lifegrid.dto.CreateEmergencyRequestDto;
import com.lifegrid.dto.EmergencyResponseDto;
import com.lifegrid.repository.AmbulanceRepository;
import com.lifegrid.repository.EmergencyRepository;
import com.lifegrid.repository.NodeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ConcurrentDispatchTest {

    @Autowired
    private EmergencyService emergencyService;

    @Autowired
    private DispatchService dispatchService;

    @Autowired
    private AmbulanceRepository ambulanceRepository;

    @Autowired
    private EmergencyRepository emergencyRepository;

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private NetworkService networkService;

    @Test
    @DisplayName("Two simultaneous emergencies dispatched concurrently receive distinct ambulances without double-assignment")
    void testSimultaneousEmergenciesDistinctAmbulances() throws InterruptedException, ExecutionException {
        // Reset network so all ambulances are clean
        networkService.resetSimulation();

        List<Node> nodes = nodeRepository.findAll();
        assertTrue(nodes.size() >= 4, "Road network must contain at least 4 nodes");
        Long sceneNode1 = nodes.get(2).getId(); // Governorpet
        Long sceneNode2 = nodes.get(3).getId(); // Besant Road

        int concurrentCalls = 2;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentCalls);
        CountDownLatch readyLatch = new CountDownLatch(concurrentCalls);
        CountDownLatch startLatch = new CountDownLatch(1);

        Future<EmergencyResponseDto> future1 = executor.submit(() -> {
            readyLatch.countDown();
            try {
                startLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return emergencyService.createAndDispatch(
                    new CreateEmergencyRequestDto("CARDIAC_ARREST", 9, sceneNode1)
            );
        });

        Future<EmergencyResponseDto> future2 = executor.submit(() -> {
            readyLatch.countDown();
            try {
                startLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return emergencyService.createAndDispatch(
                    new CreateEmergencyRequestDto("TRAUMA_ACCIDENT", 8, sceneNode2)
            );
        });

        // Ensure both threads are ready, then release trigger simultaneously
        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();

        EmergencyResponseDto resp1 = future1.get();
        EmergencyResponseDto resp2 = future2.get();

        executor.shutdown();

        assertNotNull(resp1);
        assertNotNull(resp2);

        // Both emergencies must be ASSIGNED
        assertEquals(EmergencyStatus.ASSIGNED, resp1.getStatus());
        assertEquals(EmergencyStatus.ASSIGNED, resp2.getStatus());

        // CRITICAL THREAD-SAFETY ASSERTION:
        // Different ambulances must be assigned to the two concurrent requests
        assertNotNull(resp1.getAmbulanceId());
        assertNotNull(resp2.getAmbulanceId());
        assertNotEquals(resp1.getAmbulanceId(), resp2.getAmbulanceId(),
                "Ambulance IDs must be distinct - zero double-assignment allowed");

        // Verify that both assigned ambulances have status DISPATCHED in the database
        Ambulance amb1 = ambulanceRepository.findById(resp1.getAmbulanceId()).orElseThrow();
        Ambulance amb2 = ambulanceRepository.findById(resp2.getAmbulanceId()).orElseThrow();

        assertTrue(amb1.getStatus() == AmbulanceStatus.DISPATCHED || amb1.getStatus() == AmbulanceStatus.AT_SCENE);
        assertTrue(amb2.getStatus() == AmbulanceStatus.DISPATCHED || amb2.getStatus() == AmbulanceStatus.AT_SCENE);
    }

    @Test
    @DisplayName("Autonomous triage queue: Pending emergency is automatically dispatched when an ambulance frees up")
    void testAutonomousQueueDrainOnAmbulanceFreed() throws InterruptedException {
        networkService.resetSimulation();

        List<Node> nodes = nodeRepository.findAll();
        Long sceneNode1 = nodes.get(1).getId();
        Long sceneNode2 = nodes.get(2).getId();

        // Mark all ambulances busy except one
        var ambulances = ambulanceRepository.findAll();
        for (int i = 0; i < ambulances.size() - 1; i++) {
            Ambulance amb = ambulances.get(i);
            amb.setStatus(AmbulanceStatus.DISPATCHED);
            ambulanceRepository.save(amb);
        }

        // Only 1 ambulance is AVAILABLE now
        assertEquals(1, ambulanceRepository.findByStatus(AmbulanceStatus.AVAILABLE).size());

        // Emergency 1 is assigned the sole available ambulance
        EmergencyResponseDto first = emergencyService.createAndDispatch(
                new CreateEmergencyRequestDto("CARDIAC_ARREST", 9, sceneNode1)
        );
        assertEquals(EmergencyStatus.ASSIGNED, first.getStatus());

        // Now 0 ambulances are AVAILABLE
        assertEquals(0, ambulanceRepository.findByStatus(AmbulanceStatus.AVAILABLE).size());

        // Emergency 2 arrives while fleet is fully busy -> stays PENDING in queue
        EmergencyResponseDto second = emergencyService.createAndDispatch(
                new CreateEmergencyRequestDto("MINOR_FRACTURE", 4, sceneNode2)
        );
        assertEquals(EmergencyStatus.PENDING, second.getStatus());

        // Free the first ambulance by completing its emergency
        dispatchService.completeEmergency(first.getId());

        // Give event publisher a brief moment to trigger dispatchNextPendingEmergency()
        Thread.sleep(150);

        // Emergency 2 should now have been automatically dispatched!
        Emergency updatedSecond = emergencyRepository.findById(second.getId()).orElseThrow();
        assertEquals(EmergencyStatus.ASSIGNED, updatedSecond.getStatus(),
                "Queued emergency must be automatically assigned when an ambulance becomes free");
        assertNotNull(updatedSecond.getAmbulanceId());
    }
}
