package com.lifegrid.core;

import com.lifegrid.domain.Node;
import com.lifegrid.domain.Road;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Adjacency-list representation of the road network graph.
 * <p>
 * Completely decoupled from Spring frameworks; operates as a pure data structure.
 */
public class Graph {

    public static class Edge {
        private final Long targetNodeId;
        private final double distanceKm;

        public Edge(Long targetNodeId, double distanceKm) {
            if (targetNodeId == null) {
                throw new IllegalArgumentException("Target node ID cannot be null");
            }
            if (distanceKm < 0) {
                throw new IllegalArgumentException("Distance cannot be negative");
            }
            this.targetNodeId = targetNodeId;
            this.distanceKm = distanceKm;
        }

        public Long getTargetNodeId() {
            return targetNodeId;
        }

        public double getDistanceKm() {
            return distanceKm;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge edge = (Edge) o;
            return Double.compare(edge.distanceKm, distanceKm) == 0 &&
                    Objects.equals(targetNodeId, edge.targetNodeId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(targetNodeId, distanceKm);
        }

        @Override
        public String toString() {
            return "Edge{target=" + targetNodeId + ", distanceKm=" + distanceKm + '}';
        }
    }

    private final Map<Long, List<Edge>> adjacencyList = new HashMap<>();
    private final Set<Long> nodeIds = new HashSet<>();

    public Graph() {
    }

    /**
     * Builds a graph directly from collections of domain entities.
     *
     * @param nodes collection of nodes
     * @param roads collection of connecting road corridors
     * @return constructed Graph instance
     */
    public static Graph from(Collection<Node> nodes, Collection<Road> roads) {
        Graph graph = new Graph();
        if (nodes != null) {
            for (Node node : nodes) {
                if (node != null && node.getId() != null) {
                    graph.addNode(node.getId());
                }
            }
        }
        if (roads != null) {
            for (Road road : roads) {
                if (road != null && road.getFromNode() != null && road.getToNode() != null) {
                    graph.addEdge(
                            road.getFromNode().getId(),
                            road.getToNode().getId(),
                            road.getDistanceKm(),
                            road.isBidirectional()
                    );
                }
            }
        }
        return graph;
    }

    public synchronized void addNode(Long nodeId) {
        if (nodeId == null) {
            throw new IllegalArgumentException("Node ID cannot be null");
        }
        nodeIds.add(nodeId);
        adjacencyList.putIfAbsent(nodeId, new ArrayList<>());
    }

    public synchronized void addEdge(Long fromNodeId, Long toNodeId, double distanceKm, boolean bidirectional) {
        if (fromNodeId == null || toNodeId == null) {
            throw new IllegalArgumentException("Source and target node IDs cannot be null");
        }
        addNode(fromNodeId);
        addNode(toNodeId);

        adjacencyList.get(fromNodeId).add(new Edge(toNodeId, distanceKm));
        if (bidirectional) {
            adjacencyList.get(toNodeId).add(new Edge(fromNodeId, distanceKm));
        }
    }

    public boolean containsNode(Long nodeId) {
        return nodeId != null && nodeIds.contains(nodeId);
    }

    public List<Edge> getNeighbors(Long nodeId) {
        if (!containsNode(nodeId)) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(adjacencyList.getOrDefault(nodeId, Collections.emptyList()));
    }

    public Set<Long> getNodeIds() {
        return Collections.unmodifiableSet(nodeIds);
    }

    public int getNodeCount() {
        return nodeIds.size();
    }
}
