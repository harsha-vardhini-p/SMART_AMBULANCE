package com.lifegrid;

import com.lifegrid.domain.Ambulance;
import com.lifegrid.domain.Hospital;
import com.lifegrid.domain.Node;
import com.lifegrid.domain.Road;
import com.lifegrid.repository.AmbulanceRepository;
import com.lifegrid.repository.HospitalRepository;
import com.lifegrid.repository.NodeRepository;
import com.lifegrid.repository.RoadRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class LifeGridApplicationTests {

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private RoadRepository roadRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private AmbulanceRepository ambulanceRepository;

    @Test
    @DisplayName("Context loads and DataSeeder initializes network graph, hospitals, and ambulances")
    void contextLoadsAndSeedsData() {
        List<Node> nodes = nodeRepository.findAll();
        assertEquals(18, nodes.size(), "Should seed 18 intersections in Vijayawada network");

        List<Road> roads = roadRepository.findAll();
        assertEquals(24, roads.size(), "Should seed 24 road corridors");

        List<Hospital> hospitals = hospitalRepository.findAll();
        assertEquals(4, hospitals.size(), "Should seed 4 hospitals");

        List<Ambulance> ambulances = ambulanceRepository.findAll();
        assertEquals(5, ambulances.size(), "Should seed 5 ambulances");

        // Validate hospital facilities
        Hospital ggh = hospitals.stream()
                .filter(h -> h.getName().contains("GGH"))
                .findFirst()
                .orElse(null);
        assertNotNull(ggh);
        assertTrue(ggh.hasFacility("TRAUMA"));
        assertTrue(ggh.hasFacility("CARDIAC"));

        // Validate ambulance callsigns
        boolean amb1Present = ambulances.stream().anyMatch(a -> "AMB-01".equals(a.getCallSign()));
        assertTrue(amb1Present);
    }
}
