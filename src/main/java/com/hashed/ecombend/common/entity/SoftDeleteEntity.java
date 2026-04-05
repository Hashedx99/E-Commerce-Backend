package com.hashed.ecombend.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/*
 * Abstract base for entities that support soft deletion.
 * Usage:
 *   entity.softDelete();
 *   repository.save(entity);   // sets deleted_at, entity disappears from queries
 */
@MappedSuperclass
@SQLRestriction("deleted_at IS NULL")
@Getter
public abstract class SoftDeleteEntity extends BaseEntity {

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Soft-deletes this entity by timestamping its removal.
     * Does NOT remove the record from the database.
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Restores a soft-deleted entity by clearing the deletion timestamp.
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
