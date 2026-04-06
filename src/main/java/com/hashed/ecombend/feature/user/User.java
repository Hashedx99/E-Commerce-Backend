package com.hashed.ecombend.feature.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hashed.ecombend.common.entity.SoftDeleteEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * <p>
 * Represents a platform user. Extends SoftDeleteEntity so:
 * - admin soft delete → user.softDelete() + repository.save()
 * - @SQLRestriction("deleted_at IS NULL") hides the record from all generated queries
 *
 * @Version enables optimistic locking two simultaneous profile updates
 * on the same user will result in one succeeding and one getting a 409.
 * <p>
 * Authentication identity = email address.
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"passwordHash"})
public class User extends SoftDeleteEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /**
     * BCrypt hash — NEVER the raw password.
     * WRITE_ONLY: accepted in request deserialization, never serialized into responses.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.CUSTOMER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @JsonIgnore
    @Column(name = "verification_token")
    private String verificationToken;

    @JsonIgnore
    @Column(name = "verification_token_expires_at")
    private LocalDateTime verificationTokenExpiresAt;

    @JsonIgnore
    @Column(name = "reset_token")
    private String resetToken;

    @JsonIgnore
    @Column(name = "reset_token_expires_at")
    private LocalDateTime resetTokenExpiresAt;

    /**
     * Optimistic lock version managed by JPA.
     * Protects against concurrent updates to the same user record.
     */
    @Version
    @Column(name = "version")
    private Integer version;

    /**
     * @return true if this user is allowed to authenticate.
     * Must be email verified, active status, and not soft deleted.
     */
    public boolean canLogin() {
        return this.emailVerified
                && this.status == UserStatus.ACTIVE
                && !this.isDeleted();
    }
}
