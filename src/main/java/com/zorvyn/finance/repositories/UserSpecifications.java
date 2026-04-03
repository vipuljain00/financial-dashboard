package com.zorvyn.finance.repositories;

import com.zorvyn.finance.entities.User;
import com.zorvyn.finance.enums.EntityStatus;
import com.zorvyn.finance.enums.Gender;
import com.zorvyn.finance.enums.Role;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecifications {
    private UserSpecifications() {
    }

    public static Specification<User> createdByAdminId(Long adminId) {
        return (root, query, cb) -> adminId == null ? cb.conjunction() : cb.equal(root.get("admin").get("id"), adminId);
    }

    public static Specification<User> hasGender(Gender gender) {
        return (root, query, cb) -> gender == null ? cb.conjunction() : cb.equal(root.get("gender"), gender);
    }

    public static Specification<User> hasRole(Role role) {
        return (root, query, cb) -> role == null ? cb.conjunction() : cb.equal(root.get("role"), role);
    }

    public static Specification<User> hasStatus(EntityStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    /**
     * @param softDelete true  -> status = DELETED
     *                    false -> status != DELETED
     */
    public static Specification<User> softDelete(boolean softDelete) {
        return (root, query, cb) -> softDelete
                ? cb.equal(root.get("status"), EntityStatus.DELETED)
                : cb.notEqual(root.get("status"), EntityStatus.DELETED);
    }
}

