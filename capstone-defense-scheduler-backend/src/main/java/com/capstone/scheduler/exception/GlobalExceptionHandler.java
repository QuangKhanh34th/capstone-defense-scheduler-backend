package com.capstone.scheduler.exception;

import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Bắt lỗi ResponseStatusException
    @ExceptionHandler(value = ResponseStatusException.class)
    ResponseEntity<Map<String, Object>> handlingResponseStatusException(ResponseStatusException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", exception.getStatusCode().value());
        response.put("message", exception.getReason());
        return ResponseEntity.status(exception.getStatusCode()).body(response);
    }

    // Bắt lỗi Validation
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> handlingValidation(MethodArgumentNotValidException exception) {
        String message = exception.getFieldError() != null
                ? exception.getFieldError().getDefaultMessage()
                : "Validation error";

        Map<String, Object> response = new HashMap<>();
        response.put("code", 400);
        response.put("message", message);
        return ResponseEntity.badRequest().body(response);
    }

    // Bắt lỗi sai kiểu dữ liệu
    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    ResponseEntity<Map<String, Object>> handlingTypeMismatch(MethodArgumentTypeMismatchException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 400);
        response.put("message", "Invalid parameter format: " + exception.getName());
        return ResponseEntity.badRequest().body(response);
    }

    // Bắt lỗi Sort sai trường
    @ExceptionHandler(value = PropertyReferenceException.class)
    ResponseEntity<Map<String, Object>> handlingSortError(PropertyReferenceException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 400);
        response.put("message", "Invalid sort field: " + exception.getPropertyName());
        return ResponseEntity.badRequest().body(response);
    }

    // Bắt tất cả các lỗi còn lại
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<Map<String, Object>> handlingGenericException(Exception exception) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 500);
        response.put("message", "Internal Server Error: " + exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}