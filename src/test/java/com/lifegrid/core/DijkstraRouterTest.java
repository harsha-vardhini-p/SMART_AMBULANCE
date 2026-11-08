package com.lifegrid.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for DijkstraRouter and Graph data structures.
 * Pure JUnit 5 tests without Spring Boot context dependency.
 */
class DijkstraRouterTest {

    private DijkstraRouter router;

    @BeforeEach
    void setUp() {
        router = new DijkstraRouter(40.0); // 40 km/h baseline speed
    }

    @Test
    @DisplayName("Shortest path on a simple known graph (diamond topology)")
    void testSimpleKnownGraph() {
        /*
         * Graph:
         *       (2)
         *      /   \
         *   2.0     1.0
         *    /       \
         *  (1)       (4)
         *    \       /
         *   1.5     3.0
         *      \   /
         *       (3)
         *
         * Path 1 -> 2 -> 4: 2.0 + 1.0 = 3.0 km
         * Path 1 -> 3 -> 4: 1.5 + 3.0 = 4.5 km
         * Optimal path: 1 -> 2 -> 4 with distance 3.0 km
         * At 40 km/h: (3.0 / 40.0) * 60 = 4.5 minutes
         */
        Graph graph = new Graph();
        graph.addEdge(1L, 2L, 2.0, false);
        graph.addEdge(2L, 4L, 1.0, false);
        graph.addEdge(1L, 3L, 1.5, false);
        graph.addEdge(3L, 4L, 3.0, false);

        RouteResult result = router.findShortestPath(graph, 1L, 4L);

        assertNotNull(result);
        assertEquals(3.0, result.getDistanceKm(), 0.001);
        assertEquals(4.5, result.getEstimatedMinutes(), 0.001);
        assertEquals(List.of(1L, 2L, 4L), result.getPathNodeIds());
        assertEquals(2, result.getHopCount());
    }

    @Test
    @DisplayName("Unreachable node in disconnected component throws RoutingException")
    void testUnreachableNode() {
        /*
         * Component A: 1 <-> 2
         * Component B: 3 <-> 4 (isolated)
         */
        Graph graph = new Graph();
        graph.addEdge(1L, 2L, 2.5, true);
        graph.addEdge(3L, 4L, 1.8, true);

        RoutingException exception = assertThrows(RoutingException.class, () ->
                router.findShortestPath(graph, 1L, 4L)
        );

        assertTrue(exception.getMessage().contains("No navigable route exists"));
    }

    @Test
    @DisplayName("Same start and end node returns zero distance and single-node path")
    void testSameStartAndEnd() {
        Graph graph = new Graph();
        graph.addEdge(1L, 2L, 5.0, true);

        RouteResult result = router.findShortestPath(graph, 1L, 1L);

        assertNotNull(result);
        assertEquals(0.0, result.getDistanceKm());
        assertEquals(0.0, result.getEstimatedMinutes());
        assertEquals(List.of(1L), result.getPathNodeIds());
        assertEquals(0, result.getHopCount());
    }

    @Test
    @DisplayName("Multiple equal cost paths returns optimal distance and valid path")
    void testMultipleEqualPaths() {
        /*
         * Path A: 10 -> 20 -> 40 (2.0 + 2.0 = 4.0 km)
         * Path B: 10 -> 30 -> 40 (2.0 + 2.0 = 4.0 km)
         */
        Graph graph = new Graph();
        graph.addEdge(10L, 20L, 2.0, false);
        graph.addEdge(20L, 40L, 2.0, false);
        graph.addEdge(10L, 30L, 2.0, false);
        graph.addEdge(30L, 40L, 2.0, false);

        RouteResult result = router.findShortestPath(graph, 10L, 40L);

        assertNotNull(result);
        assertEquals(4.0, result.getDistanceKm(), 0.001);
        assertEquals(6.0, result.getEstimatedMinutes(), 0.001); // (4.0 / 40.0) * 60 = 6.0 min

        // Either path is mathematically valid
        List<Long> path = result.getPathNodeIds();
        assertTrue(
                path.equals(List.of(10L, 20L, 40L)) || path.equals(List.of(10L, 30L, 40L)),
                "Path must be one of the two equal minimal routes"
        );
    }

    @Test
    @DisplayName("Invalid or non-existent node IDs throw RoutingException")
    void testInvalidNodeIds() {
        Graph graph = new Graph();
        graph.addEdge(1L, 2L, 3.0, true);

        // Missing origin
        RoutingException exOrigin = assertThrows(RoutingException.class, () ->
                router.findShortestPath(graph, 999L, 2L)
        );
        assertTrue(exOrigin.getMessage().contains("Origin node ID 999 does not exist"));

        // Missing destination
        RoutingException exDest = assertThrows(RoutingException.class, () ->
                router.findShortestPath(graph, 1L, 888L)
        );
        assertTrue(exDest.getMessage().contains("Destination node ID 888 does not exist"));

        // Null IDs
        assertThrows(RoutingException.class, () ->
                router.findShortestPath(graph, null, 2L)
        );
    }

    @Test
    @DisplayName("Traffic congestion multiplier correctly scales estimated travel time")
    void testTrafficMultiplierScaling() {
        Graph graph = new Graph();
        graph.addEdge(1L, 2L, 20.0, true); // 20 km

        // Normal traffic (1.0x): (20 / 40) * 60 = 30.0 mins
        RouteResult normal = router.findShortestPath(graph, 1L, 2L, 1.0);
        assertEquals(30.0, normal.getEstimatedMinutes(), 0.001);

        // Congested traffic (1.5x): 30.0 * 1.5 = 45.0 mins
        RouteResult heavy = router.findShortestPath(graph, 1L, 2L, 1.5);
        assertEquals(45.0, heavy.getEstimatedMinutes(), 0.001);

        // Negative/zero multiplier throws IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () ->
                router.findShortestPath(graph, 1L, 2L, 0.0)
        );
        assertThrows(IllegalArgumentException.class, () ->
                router.findShortestPath(graph, 1L, 2L, -0.5)
        );
    }
}
