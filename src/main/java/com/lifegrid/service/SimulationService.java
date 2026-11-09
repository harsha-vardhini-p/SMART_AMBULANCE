package com.lifegrid.service;

import com.lifegrid.domain.Ambulance;
import com.lifegrid.domain.AmbulanceStatus;
import com.lifegrid.domain.Emergency;
import com.lifegrid.domain.EmergencyStatus;
import com.lifegrid.domain.Hospital;
import com.lifegrid.domain.Node;
import com.lifegrid.repository.AmbulanceRepository;
import com.lifegrid.repository.EmergencyRepository;
import com.lifegrid.repository.HospitalRepository;
import com.lifegrid.repository.NodeRepository;
import com.lifegrid.repository.RoadRepository;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Background simulation engine that orchestrates real-time, node-by-node ambulance traversal.
 * <p>
 * Uses {@link ScheduledExecutorService} with configurable accelerated time scaling:
 * <ul>
 *   <li>1 simulated minute = 1 real second (configurable)</li>
 *   <li>Lifecycle transitions: DISPATCHED &rarr; AT_SCENE (3s pause) &rarr; TO_HOSPITAL &rarr; AVAILABLE</li>
 *   <li>Frees bed reservations, completes emergency tickets, and triggers autonomous dispatch of pending calls</li>
 * </ul>
 */
@Service
public class SimulationService {

    private static final Logger log = LoggerFactory.getLogger(SimulationService.class);

    private final AmbulanceRepository ambulanceRepository;
    private final EmergencyRepository emergencyRepository;
    private final HospitalRepository hospitalRepository;
    private final NodeRepository nodeRepository;
    private final RoadRepository roadRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TransactionTemplate transactionTemplate;

    private final ScheduledExecutorService scheduler;
    private final Map<Long, ScheduledFuture<?>> activeMissions = new ConcurrentHashMap<>();

    @Value("${lifegrid.simulation.sim-minute-to-real-seconds:1.0}")
    private double simMinuteToRealSeconds = 1.0;

    @Value("${lifegrid.simulation.scene-pause-seconds:3.0}")
    private double scenePauseSeconds = 3.0;

    public SimulationService(AmbulanceRepository ambulanceRepository,
                             EmergencyRepository emergencyRepository,
                             HospitalRepository hospitalRepository,
                             NodeRepository nodeRepository,
                             RoadRepository roadRepository,
                             ApplicationEventPublisher eventPublisher,
                             TransactionTemplate transactionTemplate) {
        this.ambulanceRepository = ambulanceRepository;
        this.emergencyRepository = emergencyRepository;
        this.hospitalRepository = hospitalRepository;
        this.nodeRepository = nodeRepository;
        this.roadRepository = roadRepository;
        this.eventPublisher = eventPublisher;
        this.transactionTemplate = transactionTemplate;

        // Dedicated thread pool with named daemon threads
        this.scheduler = Executors.newScheduledThreadPool(4, r -> {
            Thread t = new Thread(r, "lifegrid-sim-worker");
            t.setDaemon(true);
            return t;
        });
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down LifeGrid simulation scheduler...");
        cancelAllMissions();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public synchronized void cancelAllMissions() {
        for (Map.Entry<Long, ScheduledFuture<?>> entry : activeMissions.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isDone()) {
                entry.getValue().cancel(true);
            }
        }
        activeMissions.clear();
    }

    /**
     * Initiates node-by-node physical simulation of an ambulance responding to an emergency.
     *
     * @param emergencyId        dispatched emergency ticket ID
     * @param ambulanceId        assigned ambulance unit ID
     * @param hospitalId         target receiving hospital ID
     * @param routeToSceneIds    ordered intersection node IDs from vehicle to incident scene
     * @param routeToHospitalIds ordered intersection node IDs from incident scene to hospital
     */
    public void startMission(Long emergencyId,
                             Long ambulanceId,
                             Long hospitalId,
                             List<Long> routeToSceneIds,
                             List<Long> routeToHospitalIds) {
        // Cancel any lingering mission for this ambulance
        ScheduledFuture<?> existing = activeMissions.remove(ambulanceId);
        if (existing != null && !existing.isDone()) {
            existing.cancel(true);
        }

        List<Node> sceneNodes = loadNodesInOrder(routeToSceneIds);
        List<Node> hospitalNodes = loadNodesInOrder(routeToHospitalIds);

        log.info("Starting simulation mission for Ambulance #{}: {} scene waypoints, {} hospital waypoints",
                ambulanceId, sceneNodes.size(), hospitalNodes.size());

        // Kick off asynchronous mission sequence
        scheduleNextStep(ambulanceId, emergencyId, hospitalId, sceneNodes, hospitalNodes, 0, false);
    }

    private void scheduleNextStep(Long ambulanceId,
                                  Long emergencyId,
                                  Long hospitalId,
                                  List<Node> sceneNodes,
                                  List<Node> hospitalNodes,
                                  int currentIndex,
                                  boolean onWayToHospital) {
        if (!onWayToHospital) {
            // Phase 1: Moving to scene
            if (currentIndex < sceneNodes.size()) {
                Node targetNode = sceneNodes.get(currentIndex);
                double legDistance = (currentIndex > 0)
                        ? calculateDistance(sceneNodes.get(currentIndex - 1), targetNode)
                        : 0.1;

                long stepDelayMs = computeLegDelayMs(legDistance);

                ScheduledFuture<?> task = scheduler.schedule(() -> {
                    updateAmbulancePosition(ambulanceId, targetNode, AmbulanceStatus.DISPATCHED,
                            calculateProgress(currentIndex, sceneNodes.size(), hospitalNodes.size(), false));

                    scheduleNextStep(ambulanceId, emergencyId, hospitalId, sceneNodes, hospitalNodes, currentIndex + 1, false);
                }, stepDelayMs, TimeUnit.MILLISECONDS);

                activeMissions.put(ambulanceId, task);
            } else {
                // Arrived AT SCENE! Transition to AT_SCENE and pause
                Node sceneNode = !sceneNodes.isEmpty() ? sceneNodes.get(sceneNodes.size() - 1) : null;
                long pauseMs = computeScenePauseMs();

                log.info("Ambulance #{} arrived AT_SCENE (Node {}). Pausing for {} ms...",
                        ambulanceId, sceneNode != null ? sceneNode.getName() : "scene", pauseMs);

                updateAmbulanceStatus(ambulanceId, AmbulanceStatus.AT_SCENE, sceneNode, 50.0);

                ScheduledFuture<?> pauseTask = scheduler.schedule(() -> {
                    log.info("Ambulance #{} departed scene with patient. En route TO_HOSPITAL...", ambulanceId);
                    scheduleNextStep(ambulanceId, emergencyId, hospitalId, sceneNodes, hospitalNodes, 0, true);
                }, pauseMs, TimeUnit.MILLISECONDS);

                activeMissions.put(ambulanceId, pauseTask);
            }
        } else {
            // Phase 2: Moving from scene to hospital
            if (currentIndex < hospitalNodes.size()) {
                Node targetNode = hospitalNodes.get(currentIndex);
                double legDistance = (currentIndex > 0)
                        ? calculateDistance(hospitalNodes.get(currentIndex - 1), targetNode)
                        : 0.1;

                long stepDelayMs = computeLegDelayMs(legDistance);

                ScheduledFuture<?> task = scheduler.schedule(() -> {
                    updateAmbulancePosition(ambulanceId, targetNode, AmbulanceStatus.TO_HOSPITAL,
                            calculateProgress(currentIndex, sceneNodes.size(), hospitalNodes.size(), true));

                    scheduleNextStep(ambulanceId, emergencyId, hospitalId, sceneNodes, hospitalNodes, currentIndex + 1, true);
                }, stepDelayMs, TimeUnit.MILLISECONDS);

                activeMissions.put(ambulanceId, task);
            } else {
                // Arrived at Hospital! Complete emergency mission
                Node hospNode = !hospitalNodes.isEmpty() ? hospitalNodes.get(hospitalNodes.size() - 1) : null;
                finishMission(ambulanceId, emergencyId, hospitalId, hospNode);
            }
        }
    }

    private void finishMission(Long ambulanceId, Long emergencyId, Long hospitalId, Node hospitalNode) {
        log.info("Ambulance #{} arrived at Hospital. Finalizing mission...", ambulanceId);

        transactionTemplate.executeWithoutResult(status -> {
            // 1. Mark emergency COMPLETED
            emergencyRepository.findById(emergencyId).ifPresent(emergency -> {
                emergency.setStatus(EmergencyStatus.COMPLETED);
                emergency.setCompletedAt(LocalDateTime.now());
                emergencyRepository.save(emergency);
                log.info("Emergency #{} marked as COMPLETED.", emergencyId);
            });

            // 2. Free hospital bed reservation
            hospitalRepository.findById(hospitalId).ifPresent(hospital -> {
                hospital.setAvailableBeds(Math.min(hospital.getTotalBeds(), hospital.getAvailableBeds() + 1));
                hospitalRepository.save(hospital);
                log.info("Released bed reservation at {}. Current available beds: {}",
                        hospital.getName(), hospital.getAvailableBeds());
            });

            // 3. Relocate and restore Ambulance to AVAILABLE
            ambulanceRepository.findById(ambulanceId).ifPresent(amb -> {
                amb.setStatus(AmbulanceStatus.AVAILABLE);
                amb.setProgress(100.0);
                if (hospitalNode != null) {
                    amb.setCurrentNodeId(hospitalNode.getId());
                    amb.setLatitude(hospitalNode.getLatitude());
                    amb.setLongitude(hospitalNode.getLongitude());
                }
                ambulanceRepository.save(amb);
                log.info("Ambulance #{} ({}) is now AVAILABLE at node {}.",
                        amb.getId(), amb.getCallSign(), amb.getCurrentNodeId());
            });
        });

        activeMissions.remove(ambulanceId);

        // 4. Publish event to trigger autonomous dispatch for any pending emergencies
        eventPublisher.publishEvent(new AmbulanceFreedEvent(ambulanceId));
    }

    private void updateAmbulancePosition(Long ambulanceId, Node node, AmbulanceStatus status, double progress) {
        if (node == null) return;
        transactionTemplate.executeWithoutResult(s -> {
            ambulanceRepository.findById(ambulanceId).ifPresent(amb -> {
                amb.setStatus(status);
                amb.setCurrentNodeId(node.getId());
                amb.setLatitude(node.getLatitude());
                amb.setLongitude(node.getLongitude());
                amb.setProgress(progress);
                ambulanceRepository.save(amb);
            });
        });
    }

    private void updateAmbulanceStatus(Long ambulanceId, AmbulanceStatus status, Node node, double progress) {
        transactionTemplate.executeWithoutResult(s -> {
            ambulanceRepository.findById(ambulanceId).ifPresent(amb -> {
                amb.setStatus(status);
                amb.setProgress(progress);
                if (node != null) {
                    amb.setCurrentNodeId(node.getId());
                    amb.setLatitude(node.getLatitude());
                    amb.setLongitude(node.getLongitude());
                }
                ambulanceRepository.save(amb);
            });
        });
    }

    private List<Node> loadNodesInOrder(List<Long> nodeIds) {
        List<Node> list = new ArrayList<>();
        if (nodeIds == null) return list;
        for (Long id : nodeIds) {
            nodeRepository.findById(id).ifPresent(list::add);
        }
        return list;
    }

    private double calculateDistance(Node a, Node b) {
        if (a == null || b == null) return 1.0;
        // Euclidean distance in local city coordinate projection (km)
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);
        return dist > 0.1 ? dist : 0.8;
    }

    private long computeLegDelayMs(double distanceKm) {
        // Average speed: 40 km/h -> (distance / 40) * 60 minutes
        double simMinutes = (distanceKm / 40.0) * 60.0;
        double realSeconds = simMinutes * simMinuteToRealSeconds;
        return Math.max(50, (long) (realSeconds * 1000.0));
    }

    private long computeScenePauseMs() {
        double realSeconds = scenePauseSeconds * (simMinuteToRealSeconds <= 0.05 ? 0.05 : 1.0);
        return Math.max(50, (long) (realSeconds * 1000.0));
    }

    private double calculateProgress(int currentLeg, int sceneCount, int hospitalCount, boolean onWayToHospital) {
        int totalLegs = Math.max(1, sceneCount + hospitalCount);
        int completedLegs = onWayToHospital ? sceneCount + currentLeg : currentLeg;
        return Math.min(99.0, ((double) completedLegs / totalLegs) * 100.0);
    }

    public void setSimMinuteToRealSeconds(double simMinuteToRealSeconds) {
        this.simMinuteToRealSeconds = simMinuteToRealSeconds;
    }

    public void setScenePauseSeconds(double scenePauseSeconds) {
        this.scenePauseSeconds = scenePauseSeconds;
    }

    public double getSimMinuteToRealSeconds() {
        return simMinuteToRealSeconds;
    }

    public double getScenePauseSeconds() {
        return scenePauseSeconds;
    }

    public boolean hasActiveMission(Long ambulanceId) {
        ScheduledFuture<?> f = activeMissions.get(ambulanceId);
        return f != null && !f.isDone();
    }
}
