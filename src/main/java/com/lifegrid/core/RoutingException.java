package com.lifegrid.core;

/**
 * Custom runtime exception thrown when routing errors occur, such as
 * non-existent nodes, invalid parameters, or unreachable destinations.
 * <p>
 * Contains no framework dependencies.
 */
public class RoutingException extends RuntimeException {

    public RoutingException(String message) {
        super(message);
    }

    public RoutingException(String message, Throwable cause) {
        super(message, cause);
    }
}
