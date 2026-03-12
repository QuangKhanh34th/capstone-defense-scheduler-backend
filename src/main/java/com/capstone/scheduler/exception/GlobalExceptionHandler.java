package com.capstone.scheduler.exception;

import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@lombok.extern.slf4j.Slf4j
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

    // 3. Bắt lỗi xác thực sai (sai username/password)
    @ExceptionHandler(value = BadCredentialsException.class)
    ResponseEntity<Map<String, Object>> handlingBadCredentials(BadCredentialsException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 401);
        response.put("message", "Invalid username or password");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // 4. Bắt lỗi không tìm thấy user
    @ExceptionHandler(value = UsernameNotFoundException.class)
    ResponseEntity<Map<String, Object>> handlingUsernameNotFound(UsernameNotFoundException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 401);
        response.put("message", exception.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // 5. Bắt lỗi không có quyền truy cập
    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<Map<String, Object>> handlingAccessDenied(AccessDeniedException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 403);
        response.put("message", "Access denied. You don't have permission to access this resource.");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // 6. Bắt lỗi nghiệp vụ (duplicate username, invalid role...)
    @ExceptionHandler(value = IllegalArgumentException.class)
    ResponseEntity<Map<String, Object>> handlingIllegalArgument(IllegalArgumentException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 400);
        response.put("message", exception.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    // 7. Bắt lỗi hết hạn refresh token
    @ExceptionHandler(value = RefreshTokenExpiredException.class)
    ResponseEntity<Map<String, Object>> handlingRefreshTokenExpired(RefreshTokenExpiredException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 403);
        response.put("message", exception.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // Bắt tất cả các lỗi còn lại
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<Map<String, Object>> handlingGenericException(Exception exception) {
        log.error("Generic error: ", exception);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 500);
        response.put("message", "Internal Server Error: " + exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
