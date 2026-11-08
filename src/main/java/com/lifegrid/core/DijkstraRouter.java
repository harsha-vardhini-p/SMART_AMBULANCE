package com.lifegrid.core;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * High-performance, framework-free implementation of Dijkstra's Single-Source
 * Shortest Path algorithm for road network routing.
 * <p>
 * Computes shortest road distance, reconstructs the node sequence, and calculates
 * realistic estimated travel times based on city road speed models and optional
 * traffic congestion multipliers.
 *
 * <h3>Time and Space Complexity Analysis:</h3>
 * <ul>
 *   <li><b>Time Complexity:</b> <code>O((|V| + |E|) * log(|V|))</code>
 *     <ul>
 *       <li>Each node is extracted from the binary min-heap ({@link PriorityQueue}) at most once: <code>O(|V| * log(|V|))</code>.</li>
 *       <li>Each outgoing road edge is relaxed, inserting or updating candidate distances: <code>O(|E| * log(|V|))</code>.</li>
 *       <li>In planar urban street networks, vertex degree is tightly bounded (average degree &le; 4),
 *           making <code>|E| = O(|V|)</code> and reducing overall runtime to near-linear <code>O(|V| * log(|V|))</code>.</li>
 *     </ul>
 *   </li>
 *   <li><b>Space Complexity:</b> <code>O(|V| + |E|)</code>
 *     <ul>
 *       <li>Adjacency graph representation: <code>O(|V| + |E|)</code></li>
 *       <li>Priority queue, distance map, and predecessor backtracking maps: <code>O(|V|)</code></li>
 *     </ul>
 *   </li>
 * </ul>
 */
public class DijkstraRouter {

    public static final double DEFAULT_AVERAGE_SPEED_KMH = 40.0;
    public static final double DEFAULT_TRAFFIC_MULTIPLIER = 1.0;

    private final double averageSpeedKmh;

    public DijkstraRouter() {
        this(DEFAULT_AVERAGE_SPEED_KMH);
    }

    public DijkstraRouter(double averageSpeedKmh) {
        if (averageSpeedKmh <= 0) {
            throw new IllegalArgumentException("Average speed must be greater than zero");
        }
        this.averageSpeedKmh = averageSpeedKmh;
    }

    /**
     * Internal node entry stored in the min-heap priority queue.
     */
    private static class NodeDistance {
        final Long nodeId;
        final double distance;

        NodeDistance(Long nodeId, double distance) {
            this.nodeId = nodeId;
            this.distance = distance;
        }

        double getDistance() {
            return distance;
        }
    }

    /**
     * Finds the shortest path with default traffic conditions (multiplier = 1.0).
     */
    public RouteResult findShortestPath(Graph graph, Long startNodeId, Long endNodeId) {
        return findShortestPath(graph, startNodeId, endNodeId, DEFAULT_TRAFFIC_MULTIPLIER);
    }

    /**
     * Computes the shortest path using Dijkstra's algorithm.
     *
     * @param graph             the network graph
     * @param startNodeId       origin intersection ID
     * @param endNodeId         destination intersection ID
     * @param trafficMultiplier congestion coefficient (&gt; 0, 1.0 = normal, 1.5 = +50% delay)
     * @return calculated {@link RouteResult} containing distance, travel minutes, and node path
     * @throws RoutingException if nodes are missing, parameters are invalid, or no path exists
     */
    public RouteResult findShortestPath(Graph graph, Long startNodeId, Long endNodeId, double trafficMultiplier) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        if (startNodeId == null || endNodeId == null) {
            throw new RoutingException("Start and destination node IDs must not be null");
        }
        if (trafficMultiplier <= 0) {
            throw new IllegalArgumentException("Traffic multiplier must be strictly positive");
        }
        if (!graph.containsNode(startNodeId)) {
            throw new RoutingException("Origin node ID " + startNodeId + " does not exist in the road graph");
        }
        if (!graph.containsNode(endNodeId)) {
            throw new RoutingException("Destination node ID " + endNodeId + " does not exist in the road graph");
        }

        // Trivial case: origin is destination
        if (Objects.equals(startNodeId, endNodeId)) {
            return new RouteResult(0.0, 0.0, List.of(startNodeId));
        }

        // Dijkstra state tracking
        Map<Long, Double> shortestDistances = new HashMap<>();
        Map<Long, Long> predecessors = new HashMap<>();
        Set<Long> settledNodes = new HashSet<>();
        PriorityQueue<NodeDistance> minHeap = new PriorityQueue<>(Comparator.comparingDouble(NodeDistance::getDistance));

        shortestDistances.put(startNodeId, 0.0);
        minHeap.offer(new NodeDistance(startNodeId, 0.0));

        while (!minHeap.isEmpty()) {
            NodeDistance current = minHeap.poll();
            Long u = current.nodeId;

            if (settledNodes.contains(u)) {
                continue; // Stale queue entry
            }
            settledNodes.add(u);

            // Early exit once destination is settled with optimal distance
            if (Objects.equals(u, endNodeId)) {
                break;
            }

            for (Graph.Edge edge : graph.getNeighbors(u)) {
                Long v = edge.getTargetNodeId();
                if (settledNodes.contains(v)) {
                    continue;
                }

                double altDistance = shortestDistances.get(u) + edge.getDistanceKm();
                double currentBest = shortestDistances.getOrDefault(v, Double.POSITIVE_INFINITY);

                if (altDistance < currentBest) {
                    shortestDistances.put(v, altDistance);
                    predecessors.put(v, u);
                    minHeap.offer(new NodeDistance(v, altDistance));
                }
            }
        }

        // Verify reachability
        if (!shortestDistances.containsKey(endNodeId)) {
            throw new RoutingException(
                    "No navigable route exists between origin node " + startNodeId + " and destination node " + endNodeId
            );
        }

        // Backtrack path from destination to origin
        LinkedList<Long> path = new LinkedList<>();
        Long curr = endNodeId;
        while (curr != null) {
            path.addFirst(curr);
            curr = predecessors.get(curr);
        }

        double totalDistance = shortestDistances.get(endNodeId);
        double travelHours = (totalDistance / averageSpeedKmh) * trafficMultiplier;
        double travelMinutes = travelHours * 60.0;

        return new RouteResult(totalDistance, travelMinutes, path);
    }

    public double getAverageSpeedKmh() {
        return averageSpeedKmh;
    }
}
