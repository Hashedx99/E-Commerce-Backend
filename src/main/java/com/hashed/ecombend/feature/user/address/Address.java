package com.hashed.ecombend.feature.user.address;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hashed.ecombend.common.entity.BaseEntity;
import com.hashed.ecombend.feature.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A saved shipping address belonging to a user.
 * <p>
 * Addresses are NOT soft deleted they are hard-deleted when removed.
 * There is no audit need for deleted addresses.
 * <p>
 * When an order is placed, the selected address fields are copied
 * onto the Order entity. This means editing or deleting an address later
 * does not affect the historical record of where an order was sent.
 */
@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address extends BaseEntity {

    @Column(name = "label")
    private String label;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "line1", nullable = false)
    private String line1;

    @Column(name = "line2")
    private String line2;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(name = "country", nullable = false)
    private String country;

    /**
     * Whether this is the user's default shipping address.
     * When a new address is set as default, all others for that user are set to false
     * before saving the new one — enforced in AddressService.
     */
    @Column(name = "is_default", nullable = false)
    private boolean defaultAddress = false;

    /**
     * The owning user — not serialized into API responses to avoid recursion.
     * LAZY fetch: we don't need the full user object just to return an address.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
