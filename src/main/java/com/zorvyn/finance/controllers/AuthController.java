package com.zorvyn.finance.controllers;

import com.zorvyn.finance.dto.UserResponse;
import com.zorvyn.finance.dto.auth.AuthResponse;
import com.zorvyn.finance.dto.auth.LoginRequest;
import com.zorvyn.finance.dto.auth.RegisterRequest;
import com.zorvyn.finance.dto.auth.RefreshTokenRequest;
import com.zorvyn.finance.entities.User;
import com.zorvyn.finance.enums.EntityStatus;
import com.zorvyn.finance.enums.Gender;
import com.zorvyn.finance.enums.Role;
import com.zorvyn.finance.exceptions.AuthenticationException;
import com.zorvyn.finance.exceptions.ConflictException;
import com.zorvyn.finance.exceptions.ForbiddenException;
import com.zorvyn.finance.repositories.UserRepository;
import com.zorvyn.finance.security.CustomUserDetailsService;
import com.zorvyn.finance.security.JwtTokenProvider;
import com.zorvyn.finance.security.RolePermissionService;
import com.zorvyn.finance.services.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication endpoints (JWT access + refresh tokens).")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final RolePermissionService rolePermissionService;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.expiration.ms}")
    private long jwtExpirationMs;

    @PostMapping("/register")
    @Operation(summary = "Register a user (default role: VIEWER)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "409", description = "Email already registered", content = @Content),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    })
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Email already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setMobileNo(request.getMobileNo());
        user.setGender(Gender.valueOf(request.getGender().toUpperCase()));
        user.setRole(Role.VIEWER);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(EntityStatus.ACTIVE);

        User savedUser = userRepository.save(user);
        UserResponse userResponse = buildUserResponse(savedUser);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "User registered successfully",
                        "user", userResponse));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive access + refresh tokens")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        var userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            throw new AuthenticationException("Invalid email or password");
        }

        User user = userOptional.get();

        if (!user.getStatus().equals(EntityStatus.ACTIVE)) {
            throw new ForbiddenException("User account is inactive");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid email or password");
        }

        var userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtTokenProvider.generateToken(user, userDetails.getAuthorities());
        String refreshToken = refreshTokenService.createRefreshToken(user);
        UserResponse userResponse = buildUserResponse(user);

        AuthResponse authResponse = AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationMs / 1000)
                .user(userResponse)
                .build();

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using a refresh token")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        User user = refreshTokenService.consumeAndValidateRefreshToken(request.getRefreshToken());

        if (!user.getStatus().equals(EntityStatus.ACTIVE)) {
            throw new ForbiddenException("User account is inactive");
        }

        var userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String newAccessToken = jwtTokenProvider.generateToken(user, userDetails.getAuthorities());
        String newRefreshToken = refreshTokenService.createRefreshToken(user);

        return ResponseEntity.ok(Map.of(
                "token", newAccessToken,
                "refreshToken", newRefreshToken,
                "tokenType", "Bearer",
                "expiresIn", jwtExpirationMs / 1000));
    }

    private UserResponse buildUserResponse(User user) {
        Set<String> permissions = rolePermissionService.getPermissionsForRole(user.getRole());

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .mobileNo(user.getMobileNo())
                .role(user.getRole())
                .gender(user.getGender())
                .status(user.getStatus().toString())
                .permissions(permissions)
                .dateCreated(user.getDateCreated())
                .dateUpdated(user.getDateUpdated())
                .build();
    }
}
