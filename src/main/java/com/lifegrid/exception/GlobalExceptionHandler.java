package com.lifegrid.exception;

import com.lifegrid.core.HospitalFullException;
import com.lifegrid.core.NoAmbulanceAvailableException;
import com.lifegrid.core.RoutingException;
import com.lifegrid.dto.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception interceptor mapping system errors to consistent JSON contracts.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ErrorResponseDto response = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Input validation failed for " + errors.size() + " field(s)",
                request.getRequestURI(),
                errors
        );
        log.warn("Validation failed for {}: {}", request.getRequestURI(), errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(NoAmbulanceAvailableException.class)
    public ResponseEntity<ErrorResponseDto> handleNoAmbulance(NoAmbulanceAvailableException ex, HttpServletRequest request) {
        log.warn("Ambulance allocation conflict at {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponseDto response = new ErrorResponseDto(
                HttpStatus.CONFLICT.value(),
                "Fleet Exhausted / Incompatible",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(HospitalFullException.class)
    public ResponseEntity<ErrorResponseDto> handleHospitalFull(HospitalFullException ex, HttpServletRequest request) {
        log.warn("Hospital capacity exhausted at {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponseDto response = new ErrorResponseDto(
                HttpStatus.CONFLICT.value(),
                "Hospital Bed Capacity Exhausted",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(RoutingException.class)
    public ResponseEntity<ErrorResponseDto> handleRoutingException(RoutingException ex, HttpServletRequest request) {
        log.warn("Routing calculation error at {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponseDto response = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Routing Path Error",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument error at {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponseDto response = new ErrorResponseDto(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        log.warn("Illegal state error at {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponseDto response = new ErrorResponseDto(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled server exception at " + request.getRequestURI(), ex);
        ErrorResponseDto response = new ErrorResponseDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected server error occurred: " + ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
