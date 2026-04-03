package com.zorvyn.finance.dto.users;

import com.zorvyn.finance.enums.Gender;
import com.zorvyn.finance.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "AdminUserCreateRequest")
public record AdminUserCreateRequest(
        @NotBlank
        @Size(min = 2, max = 150)
        String fullName,

        @NotBlank
        @Email
        @Size(max = 120)
        String email,

        @Size(min = 9, max = 20)
        String mobileNo,

        @NotNull
        Gender gender,

        @NotNull
        Role role,

        @NotBlank
        @Size(min = 6, max = 255)
        String password
) {
}

