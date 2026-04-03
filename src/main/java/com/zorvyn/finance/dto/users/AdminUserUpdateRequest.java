package com.zorvyn.finance.dto.users;

import com.zorvyn.finance.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "AdminUserUpdateRequest")
public record AdminUserUpdateRequest(
        @NotBlank
        @Size(min = 2, max = 150)
        String fullName,

        @Size(min = 9, max = 20)
        String mobileNo,

        Gender gender
) {
}

