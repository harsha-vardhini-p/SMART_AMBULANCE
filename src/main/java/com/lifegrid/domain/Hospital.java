package com.lifegrid.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a medical center with specialized treatment facilities and bed capacities.
 */
@Entity
@Table(name = "hospitals")
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "node_id", nullable = false)
    private Long nodeId;

    @Column(name = "total_beds", nullable = false)
    private int totalBeds;

    @Column(name = "available_beds", nullable = false)
    private int availableBeds;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "hospital_facilities", joinColumns = @JoinColumn(name = "hospital_id"))
    @Column(name = "facility", nullable = false, length = 50)
    private Set<String> facilities = new HashSet<>();

    public Hospital() {
    }

    public Hospital(String name, Long nodeId, int totalBeds, int availableBeds, Set<String> facilities) {
        this.name = name;
        this.nodeId = nodeId;
        this.totalBeds = totalBeds;
        this.availableBeds = availableBeds;
        if (facilities != null) {
            this.facilities = new HashSet<>(facilities);
        }
    }

    public Hospital(Long id, String name, Long nodeId, int totalBeds, int availableBeds, Set<String> facilities) {
        this.id = id;
        this.name = name;
        this.nodeId = nodeId;
        this.totalBeds = totalBeds;
        this.availableBeds = availableBeds;
        if (facilities != null) {
            this.facilities = new HashSet<>(facilities);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public int getTotalBeds() {
        return totalBeds;
    }

    public void setTotalBeds(int totalBeds) {
        this.totalBeds = totalBeds;
    }

    public int getAvailableBeds() {
        return availableBeds;
    }

    public void setAvailableBeds(int availableBeds) {
        this.availableBeds = availableBeds;
    }

    public Set<String> getFacilities() {
        return facilities;
    }

    public void setFacilities(Set<String> facilities) {
        this.facilities = facilities != null ? new HashSet<>(facilities) : new HashSet<>();
    }

    public boolean hasFacility(String facility) {
        if (facility == null || facilities == null) return false;
        return facilities.stream().anyMatch(f -> f.equalsIgnoreCase(facility));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hospital hospital = (Hospital) o;
        return Objects.equals(id, hospital.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Hospital{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", nodeId=" + nodeId +
                ", availableBeds=" + availableBeds + "/" + totalBeds +
                ", facilities=" + facilities +
                '}';
    }
}
