package com.capstone.scheduler.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. Bắt lỗi ResponseStatusException
    @ExceptionHandler(value = ResponseStatusException.class)
    ResponseEntity<Map<String, Object>> handlingResponseStatusException(ResponseStatusException exception) {
        Map<String, Object> response = new HashMap<>();

        response.put("code", exception.getStatusCode().value());
        response.put("message", exception.getReason());

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(response);
    }

    // 2. Bắt lỗi Validation (@NotBlank, @Size...)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> handlingValidation(MethodArgumentNotValidException exception) {
        String message = exception.getFieldError().getDefaultMessage();

        Map<String, Object> response = new HashMap<>();
        response.put("code", 400);
        response.put("message", message);

        return ResponseEntity.badRequest().body(response);
    }
}