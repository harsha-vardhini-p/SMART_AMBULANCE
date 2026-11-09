package com.lifegrid.core;

/**
 * Thrown when no candidate hospital has available beds or capacity
 * to admit the incoming emergency patient.
 */
public class HospitalFullException extends RuntimeException {

    public HospitalFullException(String message) {
        super(message);
    }

    public HospitalFullException(String message, Throwable cause) {
        super(message, cause);
    }
}
