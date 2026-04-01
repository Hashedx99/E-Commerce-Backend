package com.hashed.ecombend.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;


/**
 * Extension of BaseEntity that adds soft-delete capability.
 * The @SQLRestriction annotation automatically filters deleted records from ALL queries.
 * and applies it universally across all major entities.
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
