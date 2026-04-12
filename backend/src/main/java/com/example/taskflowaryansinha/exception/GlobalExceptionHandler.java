package com.example.taskflowaryansinha.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles @Valid failures on @RequestBody: returns structured 400.
     * Format: {"error": "validation failed", "fields": {"fieldName": "message"}}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> fields = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid",
                        (first, second) -> first  // keep first message if the same field has multiple errors
                ));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "validation failed");
        body.put("fields", fields);
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Handles explicit ResponseStatusException throws from services.
     * 404s are normalised to {"error": "not found"}.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            body.put("error", "not found");
        } else {
            body.put("error", ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString());
        }
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }
}
