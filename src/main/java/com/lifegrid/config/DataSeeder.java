package com.lifegrid.config;

import com.lifegrid.domain.Ambulance;
import com.lifegrid.domain.AmbulanceStatus;
import com.lifegrid.domain.AmbulanceType;
import com.lifegrid.domain.Hospital;
import com.lifegrid.domain.Node;
import com.lifegrid.domain.Road;
import com.lifegrid.repository.AmbulanceRepository;
import com.lifegrid.repository.DispatchLogRepository;
import com.lifegrid.repository.EmergencyRepository;
import com.lifegrid.repository.HospitalRepository;
import com.lifegrid.repository.NodeRepository;
import com.lifegrid.repository.RoadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Seeds the initial road network graph (Vijayawada metropolitan area),
 * hospitals with facility specializations, and stationed ambulance fleet.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final NodeRepository nodeRepository;
    private final RoadRepository roadRepository;
    private final HospitalRepository hospitalRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final EmergencyRepository emergencyRepository;
    private final DispatchLogRepository dispatchLogRepository;

    public DataSeeder(NodeRepository nodeRepository,
                      RoadRepository roadRepository,
                      HospitalRepository hospitalRepository,
                      AmbulanceRepository ambulanceRepository,
                      EmergencyRepository emergencyRepository,
                      DispatchLogRepository dispatchLogRepository) {
        this.nodeRepository = nodeRepository;
        this.roadRepository = roadRepository;
        this.hospitalRepository = hospitalRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.emergencyRepository = emergencyRepository;
        this.dispatchLogRepository = dispatchLogRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (nodeRepository.count() > 0) {
            log.info("LifeGrid database already contains data ({} nodes). Skipping initial seed.", nodeRepository.count());
            return;
        }
        seedFresh();
    }

    /**
     * Resets all tables, flushes deletions, and reseeds pristine simulation state.
     */
    @Transactional
    public synchronized void resetAndSeed() {
        log.info("Purging all simulation data (dispatches, emergencies, fleet, network)...");
        dispatchLogRepository.deleteAll();
        dispatchLogRepository.flush();

        emergencyRepository.deleteAll();
        emergencyRepository.flush();

        roadRepository.deleteAll();
        roadRepository.flush();

        ambulanceRepository.deleteAll();
        ambulanceRepository.flush();

        hospitalRepository.deleteAll();
        hospitalRepository.flush();

        nodeRepository.deleteAll();
        nodeRepository.flush();

        seedFresh();
        log.info("Simulation network successfully reset and reseeded.");
    }

    @Transactional
    public void seedFresh() {
        log.info("Initializing LifeGrid simulation network with Vijayawada coordinates...");

        // 1. Seed 18 realistic intersections around Vijayawada, AP
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node("Pandit Nehru Bus Station (PNBS)", 2.1, 3.2, 16.5090, 80.6175));           // 1
        nodes.add(new Node("Vijayawada Railway Station Junction", 2.3, 4.1, 16.5182, 80.6200));       // 2
        nodes.add(new Node("Governorpet Center", 3.1, 3.6, 16.5135, 80.6280));                        // 3
        nodes.add(new Node("Besant Road Junction", 3.5, 3.4, 16.5110, 80.6320));                      // 4
        nodes.add(new Node("Prakasam Barrage", 1.0, 2.9, 16.5060, 80.6065));                          // 5
        nodes.add(new Node("Tadepalli Bypass Junction", 1.7, 0.8, 16.4855, 80.6140));                 // 6
        nodes.add(new Node("Benz Circle", 5.4, 2.3, 16.5002, 80.6515));                              // 7
        nodes.add(new Node("DV Manor / MG Road", 4.6, 2.8, 16.5045, 80.6430));                       // 8
        nodes.add(new Node("Ramavarappadu Ring", 7.4, 4.8, 16.5255, 80.6710));                        // 9
        nodes.add(new Node("Gunadala Junction", 5.9, 4.4, 16.5210, 80.6560));                         // 10
        nodes.add(new Node("Autonagar Gate", 7.5, 1.8, 16.4950, 80.6720));                            // 11
        nodes.add(new Node("Kanuru Junction", 9.2, 1.1, 16.4880, 80.6890));                          // 12
        nodes.add(new Node("Poranki Center", 10.5, 0.2, 16.4780, 80.7020));                          // 13
        nodes.add(new Node("Enikepadu Junction", 9.6, 5.4, 16.5310, 80.6930));                       // 14
        nodes.add(new Node("Bhavanipuram Hub", 0.2, 4.8, 16.5250, 80.5980));                         // 15
        nodes.add(new Node("Gollapudi One Center", -1.4, 6.2, 16.5390, 80.5820));                     // 16
        nodes.add(new Node("Currency Nagar / Ring Road", 7.2, 3.7, 16.5140, 80.6690));                // 17
        nodes.add(new Node("Gannavaram Airport Corridor", 12.8, 6.4, 16.5410, 80.7250));             // 18

        List<Node> savedNodes = nodeRepository.saveAll(nodes);
        nodeRepository.flush();
        log.info("Seeded {} road network nodes.", savedNodes.size());

        Node n1 = savedNodes.get(0);
        Node n2 = savedNodes.get(1);
        Node n3 = savedNodes.get(2);
        Node n4 = savedNodes.get(3);
        Node n5 = savedNodes.get(4);
        Node n6 = savedNodes.get(5);
        Node n7 = savedNodes.get(6);
        Node n8 = savedNodes.get(7);
        Node n9 = savedNodes.get(8);
        Node n10 = savedNodes.get(9);
        Node n11 = savedNodes.get(10);
        Node n12 = savedNodes.get(11);
        Node n13 = savedNodes.get(12);
        Node n14 = savedNodes.get(13);
        Node n15 = savedNodes.get(14);
        Node n16 = savedNodes.get(15);
        Node n17 = savedNodes.get(16);
        Node n18 = savedNodes.get(17);

        // 2. Seed 24 realistic connected road corridors (bidirectional)
        List<Road> roads = new ArrayList<>();
        roads.add(new Road(n5, n1, 1.8, true));   // Prakasam Barrage <-> PNBS
        roads.add(new Road(n1, n2, 1.5, true));   // PNBS <-> Railway Station
        roads.add(new Road(n2, n3, 1.2, true));   // Railway Station <-> Governorpet
        roads.add(new Road(n1, n3, 1.4, true));   // PNBS <-> Governorpet
        roads.add(new Road(n3, n4, 0.9, true));   // Governorpet <-> Besant Road
        roads.add(new Road(n4, n8, 1.8, true));   // Besant Road <-> DV Manor
        roads.add(new Road(n8, n7, 1.3, true));   // DV Manor <-> Benz Circle
        roads.add(new Road(n5, n6, 3.2, true));   // Prakasam Barrage <-> Tadepalli Bypass
        roads.add(new Road(n6, n7, 5.1, true));   // Tadepalli Bypass <-> Benz Circle (Varadhi)
        roads.add(new Road(n7, n11, 2.5, true));  // Benz Circle <-> Autonagar Gate
        roads.add(new Road(n11, n12, 2.2, true)); // Autonagar Gate <-> Kanuru Junction
        roads.add(new Road(n12, n13, 2.8, true)); // Kanuru Junction <-> Poranki Center
        roads.add(new Road(n7, n10, 3.1, true));  // Benz Circle <-> Gunadala Junction
        roads.add(new Road(n10, n9, 2.0, true));  // Gunadala Junction <-> Ramavarappadu Ring
        roads.add(new Road(n2, n10, 4.0, true));  // Railway Station <-> Gunadala Junction (Eluru Rd)
        roads.add(new Road(n2, n15, 3.0, true));  // Railway Station <-> Bhavanipuram Hub
        roads.add(new Road(n15, n16, 2.6, true)); // Bhavanipuram Hub <-> Gollapudi
        roads.add(new Road(n15, n5, 2.4, true));  // Bhavanipuram Hub <-> Prakasam Barrage
        roads.add(new Road(n9, n17, 1.9, true));  // Ramavarappadu Ring <-> Currency Nagar
        roads.add(new Road(n17, n11, 2.3, true)); // Currency Nagar <-> Autonagar Gate
        roads.add(new Road(n9, n14, 2.7, true));  // Ramavarappadu Ring <-> Enikepadu Junction
        roads.add(new Road(n14, n18, 4.5, true)); // Enikepadu Junction <-> Gannavaram Airport
        roads.add(new Road(n14, n12, 3.8, true)); // Enikepadu Junction <-> Kanuru Junction
        roads.add(new Road(n4, n10, 3.2, true));  // Besant Road <-> Gunadala Junction

        roadRepository.saveAll(roads);
        roadRepository.flush();
        log.info("Seeded {} interconnected road corridors.", roads.size());

        // 3. Seed 4 Hospitals with specialized care units & beds
        List<Hospital> hospitals = Arrays.asList(
                new Hospital(
                        "Government General Hospital (GGH)",
                        n10.getId(),
                        250,
                        45,
                        Set.of("TRAUMA", "CARDIAC", "GENERAL", "ICU", "PEDIATRIC")
                ),
                new Hospital(
                        "Ramesh Hospitals",
                        n7.getId(),
                        120,
                        18,
                        Set.of("CARDIAC", "TRAUMA", "ICU", "GENERAL")
                ),
                new Hospital(
                        "Manipal Hospital",
                        n6.getId(),
                        180,
                        28,
                        Set.of("TRAUMA", "CARDIAC", "GENERAL", "ICU", "PEDIATRIC")
                ),
                new Hospital(
                        "Andhra Hospitals",
                        n3.getId(),
                        90,
                        12,
                        Set.of("GENERAL", "PEDIATRIC", "ICU")
                )
        );
        hospitalRepository.saveAll(hospitals);
        hospitalRepository.flush();
        log.info("Seeded {} hospitals across Vijayawada.", hospitals.size());

        // 4. Seed 5 Ambulances placed at strategic network locations
        List<Ambulance> ambulances = Arrays.asList(
                new Ambulance(null, "AMB-01", AmbulanceType.ADVANCED, n7.getId(), AmbulanceStatus.AVAILABLE, n7.getLatitude(), n7.getLongitude()),
                new Ambulance(null, "AMB-02", AmbulanceType.BASIC, n2.getId(), AmbulanceStatus.AVAILABLE, n2.getLatitude(), n2.getLongitude()),
                new Ambulance(null, "AMB-03", AmbulanceType.ADVANCED, n10.getId(), AmbulanceStatus.AVAILABLE, n10.getLatitude(), n10.getLongitude()),
                new Ambulance(null, "AMB-04", AmbulanceType.BASIC, n6.getId(), AmbulanceStatus.AVAILABLE, n6.getLatitude(), n6.getLongitude()),
                new Ambulance(null, "AMB-05", AmbulanceType.ADVANCED, n9.getId(), AmbulanceStatus.AVAILABLE, n9.getLatitude(), n9.getLongitude())
        );
        ambulanceRepository.saveAll(ambulances);
        ambulanceRepository.flush();
        log.info("Seeded {} ambulances on standby.", ambulances.size());
    }
}
