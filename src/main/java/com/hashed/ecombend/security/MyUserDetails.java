package com.hashed.ecombend.security;

import com.hashed.ecombend.feature.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security principal wrapper around the User entity.
 * GrantedAuthority with the "ROLE_" prefix, which enables @PreAuthorize("hasRole('ADMIN')")
 * isEnabled() gates login on email verification and soft-delete status
 * unauthenticated users get a 401 automatically from Spring Security.
 */
public class MyUserDetails implements UserDetails {

    private final User user;

    public MyUserDetails(User user) {
        this.user = user;
    }

    /**
     * Returns the user's role as a Spring Security authority.
     * "ROLE_" prefix is required for hasRole() to work.
     * ADMIN → "ROLE_ADMIN", CUSTOMER → "ROLE_CUSTOMER"
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    /**
     * Email is the login identifier.
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Account is only usable if email is verified and not soft deleted.
     * Spring Security calls this during login if false, authentication fails
     * with DisabledException, which our entry point converts to a 401.
     */
    @Override
    public boolean isEnabled() {
        return user.isEmailVerified() && !user.isDeleted();
    }

    /**
     * Exposes the underlying User entity for service layer use.
     * Usage in services: SecurityUtil.getCurrentUser()
     */
    public User getUser() {
        return user;
    }
}
