package com.hashed.ecombend.feature.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Data access for User entities.
 * Spring Data JPA generates all SQL from the method names no @Query needed
 * for these standard lookups. The @SQLRestriction on SoftDeleteEntity means
 * every method here automatically excludes soft deleted users.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find by email for login and profile lookup.
     * Used by MyUserDetailsService.loadUserByUsername().
     */
    Optional<User> findByEmail(String email);

    /**
     * Uniqueness check during registration.
     */
    boolean existsByEmail(String email);

    /**
     * Find by verification token.
     */
    Optional<User> findByVerificationToken(String token);

    /**
     * Find by password reset token.
     */
    Optional<User> findByResetToken(String token);
}
