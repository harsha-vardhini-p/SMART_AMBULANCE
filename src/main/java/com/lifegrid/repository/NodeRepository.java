package com.lifegrid.repository;

import com.lifegrid.domain.Node;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NodeRepository extends JpaRepository<Node, Long> {
    Optional<Node> findByNameIgnoreCase(String name);
}
