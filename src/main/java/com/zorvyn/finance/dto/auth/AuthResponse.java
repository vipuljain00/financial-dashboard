package com.zorvyn.finance.dto.auth;

import com.zorvyn.finance.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserResponse user;
}

