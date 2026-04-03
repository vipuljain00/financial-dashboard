package com.zorvyn.finance.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zorvyn.finance.dto.common.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Writes {@link ApiErrorResponse} JSON for authenticated but unauthorized requests.
 */
@Component
@RequiredArgsConstructor
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String msg = accessDeniedException.getMessage() != null
                ? accessDeniedException.getMessage()
                : "Access denied";
        ApiErrorResponse body = ApiErrorResponse.of(HttpStatus.FORBIDDEN, msg);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
