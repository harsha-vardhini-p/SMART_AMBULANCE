package com.lifegrid.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifegrid.dto.CreateEmergencyRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LifeGridApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.lifegrid.repository.NodeRepository nodeRepository;

    @Autowired
    private com.lifegrid.service.NetworkService networkService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        networkService.resetSimulation();
    }

    @Test
    @DisplayName("POST /api/emergencies creates ticket and triggers immediate dispatch")
    void testCreateEmergencyAndDispatch() throws Exception {
        Long locationId = nodeRepository.findAll().get(2).getId();
        CreateEmergencyRequestDto request = new CreateEmergencyRequestDto("CARDIAC_ARREST", 9, locationId);

        mockMvc.perform(post("/api/emergencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.type", is("CARDIAC_ARREST")))
                .andExpect(jsonPath("$.severity", is(9)))
                .andExpect(jsonPath("$.status", is("ASSIGNED")))
                .andExpect(jsonPath("$.ambulanceId", notNullValue()))
                .andExpect(jsonPath("$.hospitalId", notNullValue()))
                .andExpect(jsonPath("$.estimatedMinutes", greaterThanOrEqualTo(0.0)))
                .andExpect(jsonPath("$.dispatchDetails.ambulanceCallSign", notNullValue()))
                .andExpect(jsonPath("$.dispatchDetails.hospitalName", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/emergencies with invalid severity or blank type returns 400 Bad Request")
    void testCreateEmergencyValidationFailure() throws Exception {
        // Severity 15 is out of 1-10 range, type is blank
        CreateEmergencyRequestDto invalidRequest = new CreateEmergencyRequestDto("", 15, null);

        mockMvc.perform(post("/api/emergencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.fieldErrors.severity", notNullValue()))
                .andExpect(jsonPath("$.fieldErrors.type", notNullValue()))
                .andExpect(jsonPath("$.fieldErrors.locationNodeId", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/nodes returns 18 network intersections")
    void testGetNodes() throws Exception {
        mockMvc.perform(get("/api/nodes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(18)))
                .andExpect(jsonPath("$[0].name", notNullValue()))
                .andExpect(jsonPath("$[0].latitude", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/roads returns 24 network road corridors")
    void testGetRoads() throws Exception {
        mockMvc.perform(get("/api/roads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(24)))
                .andExpect(jsonPath("$[0].distanceKm", greaterThanOrEqualTo(0.0)));
    }

    @Test
    @DisplayName("GET /api/ambulances returns fleet of 5 units")
    void testGetAmbulances() throws Exception {
        mockMvc.perform(get("/api/ambulances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].callSign", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/hospitals returns 4 medical centers with facilities")
    void testGetHospitals() throws Exception {
        mockMvc.perform(get("/api/hospitals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].facilities", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/reports/summary returns comprehensive metrics and bed utilisation")
    void testGetSummaryReport() throws Exception {
        mockMvc.perform(get("/api/reports/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmbulances", is(5)))
                .andExpect(jsonPath("$.hospitalUtilisation", hasSize(4)));
    }

    @Test
    @DisplayName("POST /api/simulation/reset reseeds database successfully")
    void testResetSimulation() throws Exception {
        mockMvc.perform(post("/api/simulation/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUCCESS")));
    }
}
