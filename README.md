# LifeGrid — Smart Ambulance Dispatch & Hospital Routing System

> **SIMULATION & DEMONSTRATION DISCLAIMER**  
> *LifeGrid is an academic simulation designed for college demonstration and research. All travel times, speed models, and dispatch paths are synthetic approximations calculated using urban graph modeling. It is NOT intended for real-world life-critical dispatch.*

---

## Architecture & Technology Stack

- **Backend Runtime:** Java 17, Spring Boot 3.3.4
- **Build System:** Apache Maven (with `mvnw` wrapper)
- **Database:** Spring Data JPA with H2 File-Based Storage (`./data/lifegrid`, `MODE=MySQL`)
- **API Documentation:** OpenAPI 3 / Swagger UI via `springdoc-openapi`
- **Core Algorithms:** Pure Java POJOs (Zero Spring/framework dependencies):
  - **Dijkstra's Shortest Path:** $O((|V| + |E|) \log |V|)$ using binary min-heap (`PriorityQueue`)
  - **Triage Prioritization:** PriorityQueue ordered by `severity` $\downarrow$ ($10 \to 1$), then `reportedAt` $\uparrow$
  - **Resource Optimization:** Nearest eligible ambulance with capability matching (ADVANCED required for severity $\ge 8$), capacity-aware hospital selection with facility matching and congestion penalty scoring
- **Validation & Errors:** Bean Validation (`jakarta.validation`), `@RestControllerAdvice` Global Exception Handler

---

## Quick Start & Running the Server

### 1. Build and Run
```bash
# Using the Maven wrapper
./mvnw spring-boot:run
```
The server will start on port `8080`.

### 2. Interactive Tooling URLs
- **Swagger UI Interactive API Docs:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI 3 JSON Specification:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **H2 Web Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - JDBC URL: `jdbc:h2:file:./data/lifegrid`
  - User: `sa`
  - Password: *(leave blank)*

---

## Postman & cURL API Reference Guide

Import these requests into Postman or run directly from your terminal with `curl`.

### 1. Report Emergency & Trigger Automated Dispatch
Submits an emergency ticket, triggers triage prioritization, locates the fastest arriving eligible ambulance, and reserves a bed at the best receiving hospital.

- **Method:** `POST`
- **URL:** `http://localhost:8080/api/emergencies`
- **Headers:** `Content-Type: application/json`

#### Scenario A: Critical Cardiac Incident (Severity 9 $\to$ Requires ADVANCED Ambulance)
```bash
curl -X POST http://localhost:8080/api/emergencies \
  -H "Content-Type: application/json" \
  -d '{
    "type": "CARDIAC_ARREST",
    "severity": 9,
    "locationNodeId": 3
  }'
```

**Expected Response (`201 Created`):**
```json
{
  "id": 1,
  "type": "CARDIAC_ARREST",
  "severity": 9,
  "locationNodeId": 3,
  "status": "ASSIGNED",
  "reportedAt": "2026-11-09T11:27:22.045",
  "assignedAt": "2026-11-09T11:27:22.051",
  "completedAt": null,
  "ambulanceId": 1,
  "hospitalId": 1,
  "estimatedMinutes": 12.15,
  "dispatchDetails": {
    "emergencyId": 1,
    "emergencyType": "CARDIAC_ARREST",
    "severity": 9,
    "locationNodeId": 3,
    "ambulanceId": 1,
    "ambulanceCallSign": "AMB-01",
    "ambulanceType": "ADVANCED",
    "hospitalId": 1,
    "hospitalName": "Government General Hospital (GGH)",
    "totalDistanceKm": 8.1,
    "totalEstimatedMinutes": 12.15,
    "routeNodeIds": [7, 8, 4, 3, 4, 10],
    "fallbackFacilityApplied": false,
    "assignedAt": "2026-11-09T11:27:22.051"
  }
}
```

#### Scenario B: Moderate Trauma Call (Severity 5)
```bash
curl -X POST http://localhost:8080/api/emergencies \
  -H "Content-Type: application/json" \
  -d '{
    "type": "TRAUMA_ACCIDENT",
    "severity": 5,
    "locationNodeId": 8
  }'
```

---

### 2. Validation Failure Handling (Consistent JSON Errors)
Sending an invalid payload triggers bean validation handled by `GlobalExceptionHandler`.

```bash
curl -X POST http://localhost:8080/api/emergencies \
  -H "Content-Type: application/json" \
  -d '{
    "type": "",
    "severity": 15,
    "locationNodeId": null
  }'
```

**Expected Response (`400 Bad Request`):**
```json
{
  "timestamp": "2026-11-09T11:27:22.049",
  "status": 400,
  "error": "Bad Request",
  "message": "Input validation failed for 3 field(s)",
  "path": "/api/emergencies",
  "fieldErrors": {
    "severity": "Severity score cannot exceed 10 (critical)",
    "locationNodeId": "Location node ID (intersection) is required",
    "type": "Emergency incident type is required (e.g., CARDIAC_ARREST, TRAUMA_ACCIDENT)"
  }
}
```

---

### 3. List All Emergencies
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/emergencies`

```bash
curl -X GET http://localhost:8080/api/emergencies
```

---

### 4. Get Emergency by ID
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/emergencies/1`

```bash
curl -X GET http://localhost:8080/api/emergencies/1
```

---

### 5. Mark Emergency as Completed
Transitions status to `COMPLETED`, returns the assigned ambulance to `AVAILABLE` status, and relocates the ambulance to the hospital where the patient was delivered.

- **Method:** `POST`
- **URL:** `http://localhost:8080/api/emergencies/1/complete`

```bash
curl -X POST http://localhost:8080/api/emergencies/1/complete
```

---

### 6. Get Ambulance Fleet Status
Retrieves call signs, vehicle types, current locations, and operational statuses (`AVAILABLE`, `DISPATCHED`).

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/ambulances`

```bash
curl -X GET http://localhost:8080/api/ambulances
```

**Sample Response:**
```json
[
  {
    "id": 1,
    "callSign": "AMB-01",
    "type": "ADVANCED",
    "currentNodeId": 7,
    "status": "AVAILABLE"
  },
  {
    "id": 2,
    "callSign": "AMB-02",
    "type": "BASIC",
    "currentNodeId": 2,
    "status": "AVAILABLE"
  }
]
```

---

### 7. Get Hospital Bed Capacities & Facilities
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/hospitals`

```bash
curl -X GET http://localhost:8080/api/hospitals
```

---

### 8. Get Network Topology (Intersections & Roads)
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/nodes` (18 Vijayawada intersections)
- **URL:** `http://localhost:8080/api/roads` (24 road corridors)

```bash
curl -X GET http://localhost:8080/api/nodes
curl -X GET http://localhost:8080/api/roads
```

---

### 9. Get Historical Dispatch Audit Logs
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/dispatches`

```bash
curl -X GET http://localhost:8080/api/dispatches
```

---

### 10. Analytical Simulation Summary Report
Provides KPI metrics: total dispatches, average response minutes, unassigned ticket counts, fleet status, and bed utilization per hospital.

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/reports/summary`

```bash
curl -X GET http://localhost:8080/api/reports/summary
```

**Sample Response:**
```json
{
  "totalDispatches": 4,
  "averageResponseMinutes": 9.75,
  "unassignedCount": 0,
  "totalAmbulances": 5,
  "availableAmbulances": 3,
  "dispatchedAmbulances": 2,
  "hospitalUtilisation": [
    {
      "hospitalId": 1,
      "hospitalName": "Government General Hospital (GGH)",
      "totalBeds": 250,
      "availableBeds": 44,
      "occupiedBeds": 206,
      "utilisationPercentage": 82.4
    },
    {
      "hospitalId": 2,
      "hospitalName": "Ramesh Hospitals",
      "totalBeds": 120,
      "availableBeds": 17,
      "occupiedBeds": 103,
      "utilisationPercentage": 85.83
    }
  ]
}
```

---

### 11. Reset Simulation State
Wipes all dynamic tickets and dispatches, resets all hospital beds and ambulance positions, and reseeds the original Vijayawada road network.

- **Method:** `POST`
- **URL:** `http://localhost:8080/api/simulation/reset`

```bash
curl -X POST http://localhost:8080/api/simulation/reset
```

**Expected Response (`200 OK`):**
```json
{
  "status": "SUCCESS",
  "message": "LifeGrid simulation environment successfully reseeded with initial Vijayawada network state."
}
```

---

## Running the Automated Test Suite

Run the full suite of unit, service, algorithm, and MockMvc controller tests:
```bash
./mvnw test
```
All **26 tests** execute in under 4 seconds.
