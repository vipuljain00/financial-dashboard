package com.zorvyn.finance.security;

import com.zorvyn.finance.entities.FinancialRecord;
import com.zorvyn.finance.entities.User;
import com.zorvyn.finance.enums.Role;
import org.springframework.stereotype.Component;

@Component
public class AccessControlService {

    // ---------------- USER MANAGEMENT ----------------

    public boolean canManageUsers(User user) {
        return user.getRole() == Role.ADMIN;
    }

    // ---------------- RECORD ACCESS ----------------

    public boolean canCreateRecord(User user) {
        return user.getRole() == Role.ADMIN;
    }

    public boolean canUpdateRecord(User user, FinancialRecord record) {
        // Only admins can update, and only their own records.
        return user.getRole() == Role.ADMIN
                && record.getCreatedBy() != null
                && record.getCreatedBy().getId().equals(user.getId());
    }

    public boolean canDeleteRecord(User user, FinancialRecord record) {
        // Only admins can delete, and only their own records.
        return user.getRole() == Role.ADMIN
                && record.getCreatedBy() != null
                && record.getCreatedBy().getId().equals(user.getId());
    }

    public boolean canViewRecord(User user, FinancialRecord record) {
        if (record.getCreatedBy() == null) {
            return false;
        }

        Long creatorId = record.getCreatedBy().getId();

        // Admin: can see only their own records
        if (user.getRole() == Role.ADMIN) {
            return creatorId.equals(user.getId());
        }

        // Analyst / Viewer: can see records created by their admin
        User admin = user.getAdmin();
        if (admin == null || admin.getId() == null) {
            return false;
        }

        return creatorId.equals(admin.getId());
    }

    // ---------------- DASHBOARD ----------------

    public boolean canViewDashboard(User user) {
        return user.getRole() == Role.ADMIN || user.getRole() == Role.ANALYST || user.getRole() == Role.VIEWER;
    }

    // ---------------- GENERIC ROLE HELPERS ----------------

    public boolean isAdmin(User user) {
        return user.getRole() == Role.ADMIN;
    }

    public boolean isAnalyst(User user) {
        return user.getRole() == Role.ANALYST;
    }

    public boolean isViewer(User user) {
        return user.getRole() == Role.VIEWER;
    }
}