package com.zorvyn.finance.enums;

public enum Permission {
    // Record permissions
    RECORD_CREATE,
    RECORD_READ,
    RECORD_UPDATE,
    RECORD_DELETE,

    // User permissions
    USER_CREATE,
    USER_READ,
    USER_UPDATE,
    USER_DELETE,

    // Dashboard & Analytics
    SUMMARY_READ,
    ANALYTICS_READ,

    // Admin permissions
    ROLE_MANAGE,
    PERMISSION_MANAGE
}

