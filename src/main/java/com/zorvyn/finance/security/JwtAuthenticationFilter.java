package com.zorvyn.finance.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zorvyn.finance.dto.common.ApiErrorResponse;
import com.zorvyn.finance.enums.EntityStatus;
import com.zorvyn.finance.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (jwt != null && tokenProvider.validateToken(jwt)) {
                String email = tokenProvider.getUsernameFromToken(jwt);
                List<String> permissions = tokenProvider.getPermissionsFromToken(jwt);

                // Load user to verify they still exist and are active
                var user = userRepository.findByEmail(email);

                if (user.isPresent()) {
                    var userEntity = user.get();

                    // Check if user is active
                    if (userEntity.getStatus() != EntityStatus.ACTIVE) {
                        log.warn("Non-active user attempting access: {} ({})", email, userEntity.getStatus());
                        writeJsonError(response, HttpStatus.FORBIDDEN, "User account is not active");
                        return;
                    }

                    // Convert permissions + role to GrantedAuthorities.
                    // Spring method security uses:
                    // - hasRole('ADMIN') => expects ROLE_ADMIN
                    // - hasAuthority('RECORD_CREATE') => expects RECORD_CREATE
                    List<GrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + userEntity.getRole().toString()));
                    authorities.addAll(permissions.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList()));

                    // Create authentication token
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    email,
                                    null,
                                    authorities
                            );

                    // Set additional details
                    authentication.setDetails(userEntity);

                    // Set in SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("Set user authentication for: {}", email);
                } else {
                    log.warn("User not found for email: {}", email);
                    writeJsonError(response, HttpStatus.UNAUTHORIZED, "User not found");
                    return;
                }
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
            if (getJwtFromRequest(request) != null) {
                writeJsonError(response, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void writeJsonError(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), ApiErrorResponse.of(status, message));
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken == null || bearerToken.isBlank()) {
            return null;
        }

        // Accept values like "Bearer <token>" (case-insensitive scheme) to be tolerant of clients.
        // Also accept if the client already sent only the token string (no scheme).
        // Also tolerate accidental "Bearer Bearer <token>" (double Bearer).
        String token = bearerToken.trim();
        while (token.regionMatches(true, 0, "Bearer ", 0, "Bearer ".length())) {
            token = token.substring("Bearer ".length()).trim();
        }
        if (!token.isBlank()) {
            // If caller provided raw token without the Bearer prefix.
            if (token.chars().filter(ch -> ch == '.').count() == 2) {
                return token;
            }
            // Might be a malformed header; treat as missing token.
        }

        return null;
    }
}

