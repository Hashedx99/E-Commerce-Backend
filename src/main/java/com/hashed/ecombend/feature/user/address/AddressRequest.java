package com.hashed.ecombend.feature.user.address;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for creating or updating an address.
 */
@Data
public class AddressRequest {

    private String label;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phone;

    @NotBlank(message = "Address line 1 is required")
    private String line1;

    private String line2;

    @NotBlank(message = "City is required")
    private String city;

    private String state;

    @NotBlank(message = "Postal code is required")
    private String postalCode;

    @NotBlank(message = "Country is required")
    private String country;

    /**
     * If true, all other addresses for this user will be set to non-default.
     */
    private boolean defaultAddress;
}
