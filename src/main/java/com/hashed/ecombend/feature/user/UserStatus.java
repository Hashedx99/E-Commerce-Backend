package com.hashed.ecombend.feature.user;

/**
 * Lifecycle status for a User account.
 * ACTIVE   → normal, can log in (if email verified).
 * INACTIVE → soft disabled by an admin. Cannot log in.
 */
public enum UserStatus {
    ACTIVE,
    INACTIVE
}
