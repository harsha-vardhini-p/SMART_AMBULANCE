package com.lifegrid.repository;

import com.lifegrid.domain.Road;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadRepository extends JpaRepository<Road, Long> {

    @Query("SELECT r FROM Road r WHERE r.fromNode.id = :nodeId OR r.toNode.id = :nodeId")
    List<Road> findConnectedRoads(@Param("nodeId") Long nodeId);

    @Query("SELECT r FROM Road r WHERE " +
           "(r.fromNode.id = :nodeA AND r.toNode.id = :nodeB) OR " +
           "(r.bidirectional = true AND r.fromNode.id = :nodeB AND r.toNode.id = :nodeA)")
    List<Road> findDirectRoads(@Param("nodeA") Long nodeA, @Param("nodeB") Long nodeB);
}
