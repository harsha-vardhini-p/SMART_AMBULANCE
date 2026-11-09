package com.lifegrid.service;

import com.lifegrid.config.DataSeeder;
import com.lifegrid.domain.AmbulanceStatus;
import com.lifegrid.domain.DispatchLog;
import com.lifegrid.domain.EmergencyStatus;
import com.lifegrid.dto.AmbulanceDto;
import com.lifegrid.dto.DispatchLogDto;
import com.lifegrid.dto.HospitalDto;
import com.lifegrid.dto.HospitalUtilisationDto;
import com.lifegrid.dto.NodeDto;
import com.lifegrid.dto.RoadDto;
import com.lifegrid.dto.SummaryReportDto;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service providing read access to network topology, live fleet status, hospital beds,
 * analytical reports, and simulation lifecycle resets.
 */
@Service
public class NetworkService {

    private static final Logger log = LoggerFactory.getLogger(NetworkService.class);

    private final NodeRepository nodeRepository;
    private final RoadRepository roadRepository;
    private final HospitalRepository hospitalRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final EmergencyRepository emergencyRepository;
    private final DispatchLogRepository dispatchLogRepository;
    private final DataSeeder dataSeeder;

    public NetworkService(NodeRepository nodeRepository,
                          RoadRepository roadRepository,
                          HospitalRepository hospitalRepository,
                          AmbulanceRepository ambulanceRepository,
                          EmergencyRepository emergencyRepository,
                          DispatchLogRepository dispatchLogRepository,
                          DataSeeder dataSeeder) {
        this.nodeRepository = nodeRepository;
        this.roadRepository = roadRepository;
        this.hospitalRepository = hospitalRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.emergencyRepository = emergencyRepository;
        this.dispatchLogRepository = dispatchLogRepository;
        this.dataSeeder = dataSeeder;
    }

    @Transactional(readOnly = true)
    public List<NodeDto> getAllNodes() {
        return nodeRepository.findAll().stream()
                .map(NodeDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RoadDto> getAllRoads() {
        return roadRepository.findAll().stream()
                .map(RoadDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HospitalDto> getAllHospitals() {
        return hospitalRepository.findAll().stream()
                .map(HospitalDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AmbulanceDto> getAllAmbulances() {
        return ambulanceRepository.findAll().stream()
                .map(AmbulanceDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DispatchLogDto> getAllDispatches() {
        return dispatchLogRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(DispatchLogDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SummaryReportDto getSummaryReport() {
        List<DispatchLog> dispatches = dispatchLogRepository.findAll();
        long totalDispatches = dispatches.size();

        double avgMinutes = dispatches.stream()
                .mapToDouble(DispatchLog::getEstimatedMinutes)
                .average()
                .orElse(0.0);

        long unassignedCount = emergencyRepository.findByStatus(EmergencyStatus.PENDING).size();

        long totalAmbulances = ambulanceRepository.count();
        long availableAmbulances = ambulanceRepository.findByStatus(AmbulanceStatus.AVAILABLE).size();
        long dispatchedAmbulances = ambulanceRepository.findByStatus(AmbulanceStatus.DISPATCHED).size()
                + ambulanceRepository.findByStatus(AmbulanceStatus.AT_SCENE).size()
                + ambulanceRepository.findByStatus(AmbulanceStatus.TO_HOSPITAL).size();

        List<HospitalUtilisationDto> hospitalStats = hospitalRepository.findAll().stream()
                .map(h -> new HospitalUtilisationDto(h.getId(), h.getName(), h.getTotalBeds(), h.getAvailableBeds()))
                .collect(Collectors.toList());

        return new SummaryReportDto(
                totalDispatches,
                avgMinutes,
                unassignedCount,
                totalAmbulances,
                availableAmbulances,
                dispatchedAmbulances,
                hospitalStats
        );
    }

    @Transactional
    public void resetSimulation() {
        log.info("Resetting entire LifeGrid simulation network to initial state...");
        dataSeeder.resetAndSeed();
    }
}
