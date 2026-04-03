package com.zorvyn.finance.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenResponse {

    private String token;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
}

