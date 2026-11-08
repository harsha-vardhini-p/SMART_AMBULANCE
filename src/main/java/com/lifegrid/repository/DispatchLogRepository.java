package com.lifegrid.repository;

import com.lifegrid.domain.DispatchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DispatchLogRepository extends JpaRepository<DispatchLog, Long> {

    List<DispatchLog> findByEmergencyId(Long emergencyId);

    List<DispatchLog> findAllByOrderByCreatedAtDesc();
}
