package com.zorvyn.finance.config;

import com.zorvyn.finance.dto.common.ApiErrorResponse;
import com.zorvyn.finance.exceptions.AuthenticationException;
import com.zorvyn.finance.exceptions.BadRequestException;
import com.zorvyn.finance.exceptions.ConflictException;
import com.zorvyn.finance.exceptions.ForbiddenException;
import com.zorvyn.finance.exceptions.NotFoundException;
import com.zorvyn.finance.exceptions.UnauthorizedException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(AuthenticationException e) {
        log.warn("Authentication: {}", e.getMessage());
        return build(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(UnauthorizedException e) {
        log.warn("Unauthorized: {}", e.getMessage());
        return build(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(ForbiddenException e) {
        log.warn("Forbidden: {}", e.getMessage());
        return build(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException e) {
        log.warn("Not found: {}", e.getMessage());
        return build(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException e) {
        log.warn("Conflict: {}", e.getMessage());
        return build(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException e) {
        log.warn("Bad request: {}", e.getMessage());
        return build(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> details = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid",
                        (a, b) -> a
                ));
        log.warn("Validation failed: {}", details);
        ApiErrorResponse body = ApiErrorResponse.of(HttpStatus.BAD_REQUEST, "Validation failed", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        Map<String, String> details = new HashMap<>();
        e.getConstraintViolations().forEach(v -> {
            String path = v.getPropertyPath() != null ? v.getPropertyPath().toString() : "parameter";
            details.put(path, v.getMessage());
        });
        log.warn("Constraint violation: {}", details);
        ApiErrorResponse body = ApiErrorResponse.of(HttpStatus.BAD_REQUEST, "Validation failed", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Bad request: {}", e.getMessage());
        return build(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("Malformed request body: {}", e.getMessage());
        String msg = "Malformed or unreadable request body";
        return build(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParam(MissingServletRequestParameterException e) {
        String msg = "Missing required parameter: " + e.getParameterName();
        log.warn(msg);
        return build(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String name = e.getName() != null ? e.getName() : "parameter";
        String msg = "Invalid value for '" + name + "'";
        log.warn("{}: {}", msg, e.getMessage());
        return build(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        String msg = "Method not allowed for this path";
        log.warn("{}: {}", msg, e.getMessage());
        return build(HttpStatus.METHOD_NOT_ALLOWED, msg);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoHandler(NoHandlerFoundException e) {
        return build(HttpStatus.NOT_FOUND, "No handler for " + e.getHttpMethod() + " " + e.getRequestURL());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResource(NoResourceFoundException e) {
        return build(HttpStatus.NOT_FOUND, "Resource not found: " + e.getResourcePath());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException e) {
        // Try to translate common unique-constraint violations into user-friendly messages.
        String details = null;
        try {
            Throwable mostSpecific = e.getMostSpecificCause();
            details = mostSpecific != null ? mostSpecific.getMessage() : e.getMessage();
        } catch (Exception ignored) {
            details = e.getMessage();
        }

        String msg = details != null ? details : "";
        String userMessage;
        if (msg.contains("users_email_key") || msg.toLowerCase().contains("email")) {
            userMessage = "Email already registered";
        } else if (msg.contains("users_mobile_no_key") || msg.contains("mobile_no")) {
            userMessage = "Mobile number already registered";
        } else {
            userMessage = "Request conflicts with existing data";
        }

        log.error("Data integrity violation: {}", userMessage, e);
        return build(HttpStatus.CONFLICT, userMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception e) {
        log.error("Unexpected error", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private static ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message) {
        ApiErrorResponse body = ApiErrorResponse.of(status, message);
        return ResponseEntity.status(status).body(body);
    }
}
