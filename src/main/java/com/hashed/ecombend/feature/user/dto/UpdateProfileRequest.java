package com.hashed.ecombend.feature.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * request body for PUT /api/users/profile.
 * Both fields are optional only non-null/non-blank values are applied.
 */
@Data
public class UpdateProfileRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Email(message = "Must be a valid email address")
    private String email;
}
