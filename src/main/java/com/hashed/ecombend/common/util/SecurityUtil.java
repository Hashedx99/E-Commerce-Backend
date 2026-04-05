package com.hashed.ecombend.common.util;

import com.hashed.ecombend.feature.user.User;
import com.hashed.ecombend.feature.user.UserRole;
import com.hashed.ecombend.security.MyUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility for accessing the currently authenticated user from anywhere
 * in the service layer without passing it as a method parameter.
 */
public final class SecurityUtil {

    private SecurityUtil() {
    }

    /**
     * Returns the authenticated User entity from the SecurityContext.
     *
     * @return The currently authenticated User
     * @throws ClassCastException if called outside an authenticated request context
     */
    public static User getCurrentUser() {
        return ((MyUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUser();
    }

    /**
     * @return true if the current user has the ADMIN role
     */
    public static boolean isAdmin() {
        return getCurrentUser().getRole() == UserRole.ADMIN;
    }
}
