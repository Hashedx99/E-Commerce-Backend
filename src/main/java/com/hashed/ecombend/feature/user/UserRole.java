package com.hashed.ecombend.feature.user;

/**
 * <p>
 * ADMIN    → can manage catalog, orders, users.
 * CUSTOMER → can browse, order, review.
 * <p>
 * Stored as a String in the DB (@Enumerated(EnumType.STRING)) so the value
 * is readable in the database and survives enum reordering.
 */
public enum UserRole {
    ADMIN,
    CUSTOMER
}
