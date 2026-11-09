package com.lifegrid.service;

import com.lifegrid.domain.Emergency;
import com.lifegrid.dto.CreateEmergencyRequestDto;
import com.lifegrid.dto.DispatchResponseDto;
import com.lifegrid.dto.EmergencyResponseDto;
import com.lifegrid.repository.EmergencyRepository;
import com.lifegrid.repository.NodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing emergency intake, retrieval, and immediate dispatch activation.
 */
@Service
public class EmergencyService {

    private static final Logger log = LoggerFactory.getLogger(EmergencyService.class);

    private final EmergencyRepository emergencyRepository;
    private final NodeRepository nodeRepository;
    private final DispatchService dispatchService;

    public EmergencyService(EmergencyRepository emergencyRepository,
                            NodeRepository nodeRepository,
                            DispatchService dispatchService) {
        this.emergencyRepository = emergencyRepository;
        this.nodeRepository = nodeRepository;
        this.dispatchService = dispatchService;
    }

    /**
     * Ingests a new emergency incident, validates location, saves ticket,
     * and immediately invokes automated dispatch routing.
     *
     * @param request intake payload
     * @return {@link EmergencyResponseDto} with immediate assignment and route details
     */
    @Transactional
    public EmergencyResponseDto createAndDispatch(CreateEmergencyRequestDto request) {
        log.info("Reporting new emergency: Type={}, Severity={}, NodeId={}",
                request.getType(), request.getSeverity(), request.getLocationNodeId());

        if (!nodeRepository.existsById(request.getLocationNodeId())) {
            throw new IllegalArgumentException("Location intersection node not found for ID: " + request.getLocationNodeId());
        }

        // 1. Create and persist pending emergency ticket
        Emergency emergency = new Emergency(request.getType(), request.getSeverity(), request.getLocationNodeId());
        emergency = emergencyRepository.save(emergency);

        // 2. Trigger automated triage and dispatch
        DispatchResponseDto dispatchDetails = dispatchService.dispatchEmergency(emergency.getId());

        // 3. Reload latest emergency state
        Emergency updated = emergencyRepository.findById(emergency.getId()).orElse(emergency);
        return EmergencyResponseDto.fromEntityWithDispatch(updated, dispatchDetails);
    }

    @Transactional(readOnly = true)
    public List<EmergencyResponseDto> getAllEmergencies() {
        return emergencyRepository.findAll().stream()
                .map(EmergencyResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmergencyResponseDto getEmergencyById(Long id) {
        Emergency emergency = emergencyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Emergency ticket not found for ID: " + id));
        return EmergencyResponseDto.fromEntity(emergency);
    }
}
