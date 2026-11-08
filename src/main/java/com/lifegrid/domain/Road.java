package com.lifegrid.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Represents a road segment connecting two intersections in the road graph.
 */
@Entity
@Table(name = "roads")
public class Road {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "from_node_id", nullable = false)
    private Node fromNode;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "to_node_id", nullable = false)
    private Node toNode;

    @Column(name = "distance_km", nullable = false)
    private double distanceKm;

    @Column(nullable = false)
    private boolean bidirectional = true;

    public Road() {
    }

    public Road(Node fromNode, Node toNode, double distanceKm, boolean bidirectional) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.distanceKm = distanceKm;
        this.bidirectional = bidirectional;
    }

    public Road(Long id, Node fromNode, Node toNode, double distanceKm, boolean bidirectional) {
        this.id = id;
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.distanceKm = distanceKm;
        this.bidirectional = bidirectional;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Node getFromNode() {
        return fromNode;
    }

    public void setFromNode(Node fromNode) {
        this.fromNode = fromNode;
    }

    public Node getToNode() {
        return toNode;
    }

    public void setToNode(Node toNode) {
        this.toNode = toNode;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public boolean isBidirectional() {
        return bidirectional;
    }

    public void setBidirectional(boolean bidirectional) {
        this.bidirectional = bidirectional;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Road road = (Road) o;
        return Objects.equals(id, road.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Road{" +
                "id=" + id +
                ", from=" + (fromNode != null ? fromNode.getName() : "null") +
                ", to=" + (toNode != null ? toNode.getName() : "null") +
                ", distanceKm=" + distanceKm +
                ", bidirectional=" + bidirectional +
                '}';
    }
}
