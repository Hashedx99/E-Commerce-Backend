package com.hashed.ecombend.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;


/**
 * Extension of BaseEntity that adds soft-delete capability.
 * The {@link SQLRestriction} annotation adds a default filter so that, by default,
 * Hibernate-generated queries for entities extending this class exclude rows with {@code deleted_at} set.
 * Note: this does not automatically apply to native SQL queries or other direct database access,
 * which must explicitly include the {@code deleted_at IS NULL} condition if soft-deleted rows should be excluded.
 * Usage: entity.softDelete() then repository.save(entity)
 */
@MappedSuperclass
@SQLRestriction("deleted_at IS NULL")
@Getter
public abstract class SoftDeleteEntity extends BaseEntity {

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Performs a soft delete by setting the deletion timestamp.
     * The entity is NOT removed from the database — it is hidden from queries.
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Restores a soft-deleted entity by clearing the deletion timestamp.
     * Equivalent to "reactivating" an account in the Banking system.
     */
    public void restore() {
        this.deletedAt = null;
    }

    /**
     * @return true if this entity has been soft-deleted
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
