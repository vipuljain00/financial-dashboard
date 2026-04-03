package com.zorvyn.finance.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Standard API error body: HTTP status code and human-readable message.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        int status,
        String message,
        Map<String, String> details
) {

    public static ApiErrorResponse of(HttpStatus status, String message) {
        return new ApiErrorResponse(status.value(), message, null);
    }

    public static ApiErrorResponse of(HttpStatus status, String message, Map<String, String> details) {
        return new ApiErrorResponse(status.value(), message, details);
    }
}
