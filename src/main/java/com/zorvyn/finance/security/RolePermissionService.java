package com.zorvyn.finance.security;

import com.zorvyn.finance.enums.Permission;
import com.zorvyn.finance.enums.Role;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RolePermissionService {

    public Set<String> getPermissionsForRole(Role role) {
        return switch (role) {
            case VIEWER -> getViewerPermissions();
            case ANALYST -> getAnalystPermissions();
            case ADMIN -> getAdminPermissions();
        };
    }

    private Set<String> getViewerPermissions() {
        return Set.of(
                Permission.SUMMARY_READ.toString());
    }

    private Set<String> getAnalystPermissions() {
        return Set.of(
                Permission.RECORD_READ.toString(),
                Permission.SUMMARY_READ.toString(),
                Permission.ANALYTICS_READ.toString());
    }

    private Set<String> getAdminPermissions() {
        Set<String> permissions = new HashSet<>();
        for (Permission permission : Permission.values()) {
            permissions.add(permission.toString());
        }
        return permissions;
    }
}
