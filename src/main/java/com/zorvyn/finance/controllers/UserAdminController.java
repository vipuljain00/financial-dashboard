package com.zorvyn.finance.controllers;

import com.zorvyn.finance.dto.UserResponse;
import com.zorvyn.finance.dto.common.PagedResponse;
import com.zorvyn.finance.dto.users.AdminUserCreateRequest;
import com.zorvyn.finance.dto.users.AdminUserUpdateRequest;
import com.zorvyn.finance.dto.users.UpdateUserRoleRequest;
import com.zorvyn.finance.enums.EntityStatus;
import com.zorvyn.finance.enums.Gender;
import com.zorvyn.finance.enums.Role;
import com.zorvyn.finance.services.UserAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Admin - Users", description = "Admin-only user management: create/update roles, toggle status, list & delete.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserAdminService userAdminService;

    public UserAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    @Operation(summary = "Get all users created by this ADMIN (paginated, filtered)")
    public PagedResponse<UserResponse> list(
            @ParameterObject @PageableDefault(size = 20) Pageable pageable,
            @Parameter(description = "Gender filter") @RequestParam(required = false) Gender gender,
            @Parameter(description = "Role filter") @RequestParam(required = false) Role role,
            @Parameter(description = "Status filter") @RequestParam(required = false) EntityStatus status,
            @Parameter(description = "softDelete filter: false => exclude DELETED (default), true => only DELETED")
            @RequestParam(required = false, defaultValue = "false") Boolean softDelete
    ) {
        return PagedResponse.fromPage(userAdminService.list(pageable, gender, role, status, softDelete));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by id with full details")
    public UserResponse get(@PathVariable Long id) {
        return userAdminService.get(id);
    }

    @PostMapping
    @Operation(summary = "Create a user")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody AdminUserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userAdminService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user profile fields")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody AdminUserUpdateRequest request) {
        return userAdminService.update(id, request);
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Change a user's role (admin cannot change their own role)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Cannot change own role", content = @Content)
    })
    public UserResponse updateRole(@PathVariable Long id, @Valid @RequestBody UpdateUserRoleRequest request) {
        return userAdminService.updateRole(id, request.role());
    }

    @PatchMapping("/{id}/toggle-status")
    @Operation(summary = "Toggle user status (ACTIVE <-> INACTIVE). Admin cannot toggle their own status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid operation (self, deleted user, etc.)", content = @Content)
    })
    public UserResponse toggleStatus(@PathVariable Long id) {
        return userAdminService.toggleStatus(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete user: sets status to DELETED and replaces email/mobile with date-based values")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "No Content", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userAdminService.deleteSoft(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/permanent")
    @Operation(summary = "Permanently delete user from database (hard delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "No Content", content = @Content)
    })
    public ResponseEntity<Void> deletePermanent(@PathVariable Long id) {
        userAdminService.deletePermanent(id);
        return ResponseEntity.noContent().build();
    }
}
