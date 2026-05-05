package com.demo.EmployeeDb.advice;

import java.util.*;
import com.demo.EmployeeDb.dto.ApiResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handles @Valid validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        logger.warn("400 — Validation failed: {}", errors);
        return new ApiResponse(400, "Validation failed.", errors);
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResponse handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String path = request.getRequestURI();
        String paramName = e.getName();
        String invalidValue = String.valueOf(e.getValue());

        String message = "Invalid value '" + invalidValue + "' for '" + paramName + "'. Expected a valid numeric ID.";
        logger.warn("400 — Type Mismatch at {}: {}", path, message);
        return new ApiResponse(400, message, null);
    }

    // Handles all RuntimeExceptions — Employee not found, Department not found, etc.
    @ExceptionHandler(RuntimeException.class)
    public ApiResponse handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String message = e.getMessage();
        int status;

        // Detect status code based on message content
        if (message != null && message.toLowerCase().contains("not found")) {
            status = 404;
            logger.warn("404 — Not Found: {}", message);

        } else if (message != null && message.toLowerCase().contains("already exists")) {
            status = 409;
            logger.warn("409 — Conflict: {}", message);

        } else if (message != null && message.toLowerCase().contains("invalid credentials")) {
            status = 401;
            logger.warn("401 — Unauthorized: {}", message);

        } else if (message != null && message.toLowerCase().contains("incorrect password")) {
            status = 401;
            logger.warn("401 — Unauthorized: {}", message);

        } else if (message != null && message.toLowerCase().contains("cannot delete admin")) {
            status = 403;
            logger.warn("403 — Forbidden: {}", message);

        } else if (message != null && message.toLowerCase().contains("required")) {
            status = 400;
            logger.warn("400 — Bad Request: {}", message);

        } else {
            status = 500;
            logger.error("500 — Internal Server Error: {}", message);
        }

        return new ApiResponse(status, message, null);
    }

    // Handles access denied — wrong role trying to access endpoint
    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponse handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        logger.warn("403 — Access Denied for path: {}", request.getRequestURI());
        return new ApiResponse(403, "Access Denied: You do not have permission to perform this action.", null);
    }

    // Handles all other unexpected exceptions
    @ExceptionHandler(Exception.class)
    public ApiResponse handleGenericException(Exception e, HttpServletRequest request) {
        logger.error("500 — Unexpected error at {}: {}", request.getRequestURI(), e.getMessage(), e);
        return new ApiResponse(500, "An unexpected error occurred: " + e.getMessage(), null);
    }
}