package com.facebook.config;

import com.facebook.exception.InvalidTokenException;
import com.facebook.exception.NotFoundException;
import com.facebook.util.ResponseHandler;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Constraint violation exception [ConstraintViolationException]: {}", ex.getMessage());

        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));
        return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, true, "Validation failed", errors);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Validation exception [MethodArgumentNotValidException]: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, true, "Validation failed", errors);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(NotFoundException e) {
        log.warn("Not found exception [NotFoundException]: {}", e.getMessage());

        return ResponseHandler.generateResponse(HttpStatus.NOT_FOUND, true, e.getMessage(), null);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Object> handleInvalidTokenException(InvalidTokenException e) {
        log.warn("Invalid token exception [InvalidTokenException]: {}", e.getMessage());

        return ResponseHandler.generateResponse(HttpStatus.UNAUTHORIZED, true, e.getMessage(), null);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<Object> handleInvalidFormatException(InvalidFormatException e) {
        log.warn("Invalid format exception [InvalidFormatException]: {}", e.getMessage());

        return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, true, e.getMessage(), null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Illegal Argument Exception [IllegalArgumentException]: {}", e.getMessage());

        return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, true, e.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception e) {
        log.error("Internal server error [Exception]: {}", e.getMessage());

        return ResponseHandler.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, true, e.getMessage(), null);
    }
}