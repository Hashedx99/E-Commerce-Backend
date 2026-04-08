package com.hashed.ecombend.feature.user;

import com.hashed.ecombend.common.response.ApiResponse;
import com.hashed.ecombend.feature.auth.AuthService;
import com.hashed.ecombend.feature.auth.dto.ChangePasswordRequest;
import com.hashed.ecombend.feature.user.address.Address;
import com.hashed.ecombend.feature.user.address.AddressRequest;
import com.hashed.ecombend.feature.user.dto.UpdateProfileRequest;
import com.hashed.ecombend.feature.user.dto.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * All endpoints require a valid JWT enforced by SecurityConfiguration.
 * Admin-only endpoints use @PreAuthorize("hasRole('ADMIN')").
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Users", description = "Profile management, addresses, admin user operations")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/api/users/profile")
    @Operation(summary = "Get my profile")
    public ApiResponse<UserProfileResponse> getMyProfile() {
        return ApiResponse.ok("Profile retrieved", userService.getMyProfile());
    }

    @PutMapping("/api/users/profile")
    @Operation(summary = "Update my profile")
    public ApiResponse<UserProfileResponse> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok("Profile updated", userService.updateMyProfile(request));
    }

    @PostMapping(value = "/api/users/profile/picture",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload profile picture")
    public ApiResponse<UserProfileResponse> uploadProfilePicture(
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.ok("Profile picture updated", userService.uploadProfilePicture(file));
    }

    @PutMapping("/api/users/change-password")
    @Operation(summary = "Change my password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ApiResponse.ok("Password changed successfully");
    }

    @GetMapping("/api/users/addresses")
    @Operation(summary = "Get my saved addresses")
    public ApiResponse<List<Address>> getMyAddresses() {
        return ApiResponse.ok("Addresses retrieved", userService.getMyAddresses());
    }

    @PostMapping("/api/users/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a new address")
    public ApiResponse<Address> createAddress(@Valid @RequestBody AddressRequest request) {
        return ApiResponse.ok("Address created", userService.createAddress(request));
    }

    @PutMapping("/api/users/addresses/{id}")
    @Operation(summary = "Update an address")
    public ApiResponse<Address> updateAddress(
            @PathVariable UUID id,
            @Valid @RequestBody AddressRequest request) {
        return ApiResponse.ok("Address updated", userService.updateAddress(id, request));
    }

    @DeleteMapping("/api/users/addresses/{id}")
    @Operation(summary = "Delete an address")
    public ApiResponse<Void> deleteAddress(@PathVariable UUID id) {
        userService.deleteAddress(id);
        return ApiResponse.ok("Address deleted");
    }

    @GetMapping("/api/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users [ADMIN]")
    public ApiResponse<List<UserProfileResponse>> getAllUsers() {
        return ApiResponse.ok("Users retrieved", userService.getAllUsers());
    }

    /**
     * soft delete. Sets deleted_at on the user they can no longer log in.
     * The @SQLRestriction on User makes them invisible to all other generated queries.
     */
    @DeleteMapping("/api/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a user [ADMIN]")
    public ApiResponse<Void> softDeleteUser(@PathVariable UUID id) {
        userService.softDeleteUser(id);
        return ApiResponse.ok("User deactivated");
    }
}
