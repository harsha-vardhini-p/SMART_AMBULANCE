package com.lifegrid.service;

import com.lifegrid.core.DijkstraRouter;
import com.lifegrid.core.DispatchDecision;
import com.lifegrid.core.DispatchEngine;
import com.lifegrid.core.DispatchScoringConfig;
import com.lifegrid.core.Graph;
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
import com.lifegrid.repository.NodeRepository;
import com.lifegrid.repository.RoadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

/**
 * Orchestrates emergency dispatch workflows, loading real-time network state,
 * delegating optimization to {@link DispatchEngine}, and transactionally persisting
 * state transitions and historical dispatch logs.
 */
@Service
public class DispatchService {

    private static final Logger log = LoggerFactory.getLogger(DispatchService.class);

    private final EmergencyRepository emergencyRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final HospitalRepository hospitalRepository;
    private final NodeRepository nodeRepository;
    private final RoadRepository roadRepository;
    private final DispatchLogRepository dispatchLogRepository;

    private final DispatchEngine dispatchEngine;
    private final DijkstraRouter dijkstraRouter;
    private final DispatchScoringConfig scoringConfig;

    public DispatchService(EmergencyRepository emergencyRepository,
                           AmbulanceRepository ambulanceRepository,
                           HospitalRepository hospitalRepository,
                           NodeRepository nodeRepository,
                           RoadRepository roadRepository,
                           DispatchLogRepository dispatchLogRepository) {
        this.emergencyRepository = emergencyRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.hospitalRepository = hospitalRepository;
        this.nodeRepository = nodeRepository;
        this.roadRepository = roadRepository;
        this.dispatchLogRepository = dispatchLogRepository;

        // Initialize pure core algorithm components
        this.dispatchEngine = new DispatchEngine();
        this.dijkstraRouter = new DijkstraRouter(40.0);
        this.scoringConfig = new DispatchScoringConfig(1.0, 4.0, 5);
    }

    /**
     * Executes dispatch for a specific pending emergency ticket.
     *
     * @param emergencyId the ID of the pending emergency
     * @return {@link DispatchResponseDto} containing full assignment and routing details
     */
    @Transactional
    public DispatchResponseDto dispatchEmergency(Long emergencyId) {
        return dispatchEmergency(emergencyId, DijkstraRouter.DEFAULT_TRAFFIC_MULTIPLIER);
    }

    /**
     * Executes dispatch for a specific pending emergency ticket with a traffic multiplier.
     */
    @Transactional
    public DispatchResponseDto dispatchEmergency(Long emergencyId, double trafficMultiplier) {
        log.info("Processing dispatch request for Emergency ID: {} (Traffic Multiplier: {})", emergencyId, trafficMultiplier);

        Emergency emergency = emergencyRepository.findById(emergencyId)
                .orElseThrow(() -> new IllegalArgumentException("Emergency ticket not found for ID: " + emergencyId));

        if (emergency.getStatus() != EmergencyStatus.PENDING) {
            throw new IllegalStateException("Emergency " + emergencyId + " is already in status: " + emergency.getStatus());
        }

        // Build current road network graph
        Graph graph = Graph.from(nodeRepository.findAll(), roadRepository.findAll());

        // Fetch candidate fleet and hospital resources
        List<Ambulance> ambulances = ambulanceRepository.findAll();
        List<Hospital> hospitals = hospitalRepository.findAll();

        // Run pure Java optimization engine
        DispatchDecision decision = dispatchEngine.dispatch(
                emergency,
                ambulances,
                hospitals,
                graph,
                dijkstraRouter,
                scoringConfig,
                trafficMultiplier
        );

        Ambulance chosenAmbulance = decision.getAmbulance();
        Hospital chosenHospital = decision.getHospital();

        // 1. Update Emergency state
        emergency.setStatus(EmergencyStatus.ASSIGNED);
        emergency.setAmbulanceId(chosenAmbulance.getId());
        emergency.setHospitalId(chosenHospital.getId());
        emergency.setAssignedAt(LocalDateTime.now());
        emergency.setEstimatedMinutes(decision.getTotalEstimatedMinutes());
        emergencyRepository.save(emergency);

        // 2. Update Ambulance state
        chosenAmbulance.setStatus(AmbulanceStatus.DISPATCHED);
        ambulanceRepository.save(chosenAmbulance);

        // 3. Decrement available hospital beds
        chosenHospital.setAvailableBeds(Math.max(0, chosenHospital.getAvailableBeds() - 1));
        hospitalRepository.save(chosenHospital);

        // 4. Record DispatchLog audit entry
        DispatchLog dispatchLog = new DispatchLog(
                emergency.getId(),
                chosenAmbulance.getId(),
                chosenHospital.getId(),
                decision.getCombinedRouteNodeIds().toString(),
                decision.getTotalEstimatedMinutes()
        );
        dispatchLogRepository.save(dispatchLog);

        log.info("Successfully dispatched {} to Emergency #{} (Severity: {}). Target Hospital: {} (Route: {} nodes, Est: {} mins)",
                chosenAmbulance.getCallSign(),
                emergency.getId(),
                emergency.getSeverity(),
                chosenHospital.getName(),
                decision.getCombinedRouteNodeIds().size(),
                decision.getTotalEstimatedMinutes());

        return mapToDto(decision);
    }

    /**
     * Finds and dispatches the highest-priority pending emergency according to triage rules.
     *
     * @return {@link Optional} containing dispatch result, or empty if no emergencies are pending
     */
    @Transactional
    public Optional<DispatchResponseDto> dispatchNextPendingEmergency() {
        List<Emergency> pending = emergencyRepository.findByStatus(EmergencyStatus.PENDING);
        if (pending.isEmpty()) {
            log.info("No pending emergencies to dispatch.");
            return Optional.empty();
        }

        PriorityQueue<Emergency> queue = dispatchEngine.createEmergencyQueue(pending);
        Emergency highestPriority = queue.poll();

        if (highestPriority == null) {
            return Optional.empty();
        }

        log.info("Dispatching highest priority pending emergency: #{} (Severity: {}, Reported: {})",
                highestPriority.getId(), highestPriority.getSeverity(), highestPriority.getReportedAt());

        return Optional.of(dispatchEmergency(highestPriority.getId()));
    }

    /**
     * Marks an active emergency as completed, transitioning the ambulance back to AVAILABLE
     * stationed at the receiving hospital's intersection.
     */
    @Transactional
    public void completeEmergency(Long emergencyId) {
        log.info("Completing emergency ticket #{}", emergencyId);

        Emergency emergency = emergencyRepository.findById(emergencyId)
                .orElseThrow(() -> new IllegalArgumentException("Emergency not found for ID: " + emergencyId));

        if (emergency.getStatus() == EmergencyStatus.COMPLETED) {
            log.warn("Emergency #{} is already completed.", emergencyId);
            return;
        }

        emergency.setStatus(EmergencyStatus.COMPLETED);
        emergency.setCompletedAt(LocalDateTime.now());
        emergencyRepository.save(emergency);

        // Free the assigned ambulance if present
        if (emergency.getAmbulanceId() != null) {
            ambulanceRepository.findById(emergency.getAmbulanceId()).ifPresent(amb -> {
                amb.setStatus(AmbulanceStatus.AVAILABLE);

                // Relocate ambulance to the hospital where patient was delivered
                if (emergency.getHospitalId() != null) {
                    hospitalRepository.findById(emergency.getHospitalId()).ifPresent(hosp -> {
                        amb.setCurrentNodeId(hosp.getNodeId());
                    });
                }
                ambulanceRepository.save(amb);
                log.info("Ambulance {} is now AVAILABLE at node {}.", amb.getCallSign(), amb.getCurrentNodeId());
            });
        }

        log.info("Emergency #{} marked as COMPLETED.", emergencyId);
    }

    private DispatchResponseDto mapToDto(DispatchDecision decision) {
        return new DispatchResponseDto(
                decision.getEmergency().getId(),
                decision.getEmergency().getType(),
                decision.getEmergency().getSeverity(),
                decision.getEmergency().getLocationNodeId(),
                decision.getAmbulance().getId(),
                decision.getAmbulance().getCallSign(),
                decision.getAmbulance().getType().name(),
                decision.getHospital().getId(),
                decision.getHospital().getName(),
                decision.getTotalDistanceKm(),
                decision.getTotalEstimatedMinutes(),
                decision.getCombinedRouteNodeIds(),
                decision.isFallbackFacilityApplied(),
                decision.getEmergency().getAssignedAt()
        );
    }
}
