package com.zorvyn.finance.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zorvyn.finance.dto.common.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Writes {@link ApiErrorResponse} JSON for unauthenticated requests (filter chain).
 */
@Component
@RequiredArgsConstructor
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String msg = authException.getMessage() != null ? authException.getMessage() : "Authentication required";
        ApiErrorResponse body = ApiErrorResponse.of(HttpStatus.UNAUTHORIZED, msg);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
