package com.hashed.ecombend.feature.user.dto;

import com.hashed.ecombend.feature.user.User;
import com.hashed.ecombend.feature.user.UserRole;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * response DTO for GET /api/users/profile.
 * Never return the raw User entity from a controller it exposes internal
 * fields like passwordHash (even with @JsonProperty WRITE_ONLY, DTOs are safer).
 * This DTO contains exactly what the client needs and nothing more.
 */
@Data
public class UserProfileResponse {

    private UUID id;
    private String name;
    private String email;
    private UserRole role;
    private String profilePictureUrl;
    private LocalDateTime createdAt;

    /**
     * Factory method converts a User entity to this response DTO.
     * Call this in the service layer before returning to the controller.
     *
     * @param user The authenticated User entity
     * @return Populated UserProfileResponse
     */
    public static UserProfileResponse from(User user) {
        UserProfileResponse dto = new UserProfileResponse();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
