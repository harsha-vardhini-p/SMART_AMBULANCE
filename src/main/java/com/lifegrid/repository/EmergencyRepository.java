package com.lifegrid.repository;

import com.lifegrid.domain.Emergency;
import com.lifegrid.domain.EmergencyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyRepository extends JpaRepository<Emergency, Long> {

    List<Emergency> findByStatus(EmergencyStatus status);

    List<Emergency> findByStatusOrderBySeverityDescReportedAtAsc(EmergencyStatus status);
}
