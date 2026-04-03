package com.zorvyn.finance.security;

import com.zorvyn.finance.entities.User;
import com.zorvyn.finance.exceptions.NotFoundException;
import com.zorvyn.finance.exceptions.UnauthorizedException;
import com.zorvyn.finance.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final HttpServletRequest request;

    public User getCurrentUser() {
        // First try to get from SecurityContext (preferred)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = (String) authentication.getPrincipal();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("User not found"));
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing Authorization header");
        }

        String token = header.replace("Bearer ", "");
        String email = jwtTokenProvider.getUsernameFromToken(token);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return user.getId();
    }
}