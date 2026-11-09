package com.lifegrid.core;

/**
 * Thrown when no ambulance can be allocated to an emergency
 * (e.g., all units are busy or no ADVANCED unit is available for high-severity calls).
 */
public class NoAmbulanceAvailableException extends RuntimeException {

    public NoAmbulanceAvailableException(String message) {
        super(message);
    }

    public NoAmbulanceAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
