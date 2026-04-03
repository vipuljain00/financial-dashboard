package com.zorvyn.finance.dto.users;

import com.zorvyn.finance.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "UpdateUserRoleRequest")
public record UpdateUserRoleRequest(
        @NotNull Role role
) {
}

