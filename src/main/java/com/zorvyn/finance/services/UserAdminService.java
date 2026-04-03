package com.zorvyn.finance.services;

import com.zorvyn.finance.dto.UserResponse;
import com.zorvyn.finance.dto.users.AdminUserCreateRequest;
import com.zorvyn.finance.dto.users.AdminUserUpdateRequest;
import com.zorvyn.finance.entities.User;
import com.zorvyn.finance.enums.Gender;
import com.zorvyn.finance.enums.EntityStatus;
import com.zorvyn.finance.enums.Role;
import com.zorvyn.finance.exceptions.BadRequestException;
import com.zorvyn.finance.exceptions.ConflictException;
import com.zorvyn.finance.exceptions.ForbiddenException;
import com.zorvyn.finance.exceptions.NotFoundException;
import com.zorvyn.finance.repositories.UserSpecifications;
import com.zorvyn.finance.repositories.UserRepository;
import com.zorvyn.finance.security.CurrentUserService;
import com.zorvyn.finance.security.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolePermissionService rolePermissionService;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public Page<UserResponse> list(
            Pageable pageable,
            Gender gender,
            Role role,
            EntityStatus status,
            Boolean softDelete
    ) {
        User admin = getCurrentAdminOrThrow();

        var spec = UserSpecifications.createdByAdminId(admin.getId())
                .and(UserSpecifications.hasGender(gender))
                .and(UserSpecifications.hasRole(role));

        // If status is explicitly provided, it overrides softDelete.
        if (status != null) {
            spec = spec.and(UserSpecifications.hasStatus(status));
        } else {
            // Default: hide DELETED unless softDelete=true.
            boolean includeDeleted = Boolean.TRUE.equals(softDelete);
            spec = spec.and(UserSpecifications.softDelete(includeDeleted));
        }

        return userRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        User admin = getCurrentAdminOrThrow();
        User target = requireAdminScopedUser(admin, id);
        return toResponse(target);
    }

    @Transactional
    public UserResponse create(AdminUserCreateRequest request) {
        User admin = getCurrentAdminOrThrow();

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ConflictException("Email already registered");
        }

        if (request.mobileNo() != null && !request.mobileNo().isBlank()) {
            userRepository.findByMobileNo(request.mobileNo()).ifPresent(existing -> {
                // Allow reuse if the only match is a soft-deleted user
                if (existing.getStatus() != EntityStatus.DELETED) {
                    throw new ConflictException("Mobile number already registered");
                }
            });
        }

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setMobileNo(request.mobileNo());
        user.setGender(request.gender());
        user.setRole(request.role());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setAdmin(admin);

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, AdminUserUpdateRequest request) {
        User admin = getCurrentAdminOrThrow();
        User user = requireAdminScopedUser(admin, id);

        if (user.getStatus() == EntityStatus.DELETED) {
            throw new BadRequestException("Cannot update a deleted user");
        }

        if (request.mobileNo() != null && !request.mobileNo().isBlank()) {
            userRepository.findByMobileNo(request.mobileNo()).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId()) && existing.getStatus() != EntityStatus.DELETED) {
                    throw new ConflictException("Mobile number already registered");
                }
            });
        }

        user.setFullName(request.fullName());
        user.setMobileNo(request.mobileNo());
        if (request.gender() != null) {
            user.setGender(request.gender());
        }
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateRole(Long id, Role role) {
        User admin = getCurrentAdminOrThrow();
        if (admin.getId().equals(id)) { // prevent self role changes
            throw new BadRequestException("Admin cannot change their own role");
        }
        User user = requireAdminScopedUser(admin, id);

        if (user.getStatus() == EntityStatus.DELETED) {
            throw new BadRequestException("Cannot update role of a deleted user");
        }

        user.setRole(role);
        return toResponse(userRepository.save(user));
    }

    /**
     * Toggles status ACTIVE <-> INACTIVE. Admin cannot target their own account.
     */
    @Transactional
    public UserResponse toggleStatus(Long id) {
        User admin = getCurrentAdminOrThrow();
        if (admin.getId().equals(id)) {
            throw new BadRequestException("Admin cannot toggle their own status");
        }

        User user = requireAdminScopedUser(admin, id);

        if (user.getStatus() == EntityStatus.DELETED) {
            throw new BadRequestException("Deleted users cannot be toggled");
        }

        if (user.getStatus() == EntityStatus.ACTIVE) {
            user.setStatus(EntityStatus.INACTIVE);
        } else if (user.getStatus() == EntityStatus.INACTIVE) {
            user.setStatus(EntityStatus.ACTIVE);
        } else {
            throw new BadRequestException("Invalid current status for toggle");
        }

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteSoft(Long id) {
        User admin = getCurrentAdminOrThrow();
        User user = requireAdminScopedUser(admin, id);

        if (admin.getId().equals(id)) {
            throw new BadRequestException("Admin cannot delete their own account");
        }

        if (user.getStatus() == EntityStatus.DELETED) {
            throw new BadRequestException("User is already deleted");
        }

        // Replace email + mobile so they can be reused for new accounts.
        String datePart = LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.BASIC_ISO_DATE);

        user.setEmail("deleted+" + datePart + "+" + user.getId() + "@deleted.local");

        // Keep this digits-only and <= 20 characters.
        long suffix = Math.abs(user.getId() % 1_000_000_000L); // 9 digits
        user.setMobileNo(datePart + String.format("%09d", suffix));

        user.setStatus(EntityStatus.DELETED);
        userRepository.save(user);
    }

    @Transactional
    public void deletePermanent(Long id) {
        User admin = getCurrentAdminOrThrow();
        User user = requireAdminScopedUser(admin, id);

        if (user.getStatus() != EntityStatus.DELETED) {
            throw new BadRequestException("User must be soft-deleted before permanent deletion");
        }

        userRepository.delete(user);
    }

    private User getCurrentAdminOrThrow() {
        User current = currentUserService.getCurrentUser();
        if (current.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Forbidden");
        }
        return current;
    }

    private User requireAdminScopedUser(User admin, Long id) {
        User target = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));

        // Self is allowed for read/update, but some operations still block self explicitly.
        if (target.getId() != null && target.getId().equals(admin.getId())) {
            return target;
        }

        if (target.getAdmin() == null || target.getAdmin().getId() == null || !target.getAdmin().getId().equals(admin.getId())) {
            throw new ForbiddenException("You can only manage users created by you");
        }

        return target;
    }

    private UserResponse toResponse(User user) {
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
