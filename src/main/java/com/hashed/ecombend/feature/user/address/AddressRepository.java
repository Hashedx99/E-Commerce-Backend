package com.hashed.ecombend.feature.user.address;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {

    /**
     * Returns all addresses for a given user, ordered by default first.
     */
    List<Address> findByUserIdOrderByDefaultAddressDesc(UUID userId);

    /**
     * Finds a specific address only if it belongs to the given user — prevents accessing other users' addresses.
     */
    Optional<Address> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Checks if a user has any addresses at all.
     */
    boolean existsByUserId(UUID userId);

    /**
     * Clears the default flag on all addresses for a user before setting a new default.
     * Called in AddressService before setting isDefault = true on the target address.
     */
    @Modifying
    @Query("UPDATE Address a SET a.defaultAddress = false WHERE a.user.id = :userId")
    void clearDefaultForUser(@Param("userId") UUID userId);
}
