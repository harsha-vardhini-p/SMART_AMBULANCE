package com.lifegrid.repository;

import com.lifegrid.domain.Ambulance;
import com.lifegrid.domain.AmbulanceStatus;
import com.lifegrid.domain.AmbulanceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AmbulanceRepository extends JpaRepository<Ambulance, Long> {

    List<Ambulance> findByStatus(AmbulanceStatus status);

    List<Ambulance> findByStatusAndType(AmbulanceStatus status, AmbulanceType type);

    Optional<Ambulance> findByCallSign(String callSign);
}
