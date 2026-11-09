package com.lifegrid.service;

/**
 * Event published when an ambulance finishes its transport mission and returns to AVAILABLE status.
 */
public class AmbulanceFreedEvent {

    private final Long ambulanceId;

    public AmbulanceFreedEvent(Long ambulanceId) {
        this.ambulanceId = ambulanceId;
    }

    public Long getAmbulanceId() {
        return ambulanceId;
    }
}
