package com.hashed.ecombend.feature.user;

import com.hashed.ecombend.feature.user.address.Address;
import com.hashed.ecombend.feature.user.address.AddressRequest;
import com.hashed.ecombend.feature.user.dto.UpdateProfileRequest;
import com.hashed.ecombend.feature.user.dto.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface UserService {

    /**
     * returns the profile of the currently authenticated user.
     */
    UserProfileResponse getMyProfile();

    /**
     * updates name and/or email for the currently authenticated user.
     */
    UserProfileResponse updateMyProfile(UpdateProfileRequest request);

    /**
     * uploads and sets a profile picture for the current user.
     */
    UserProfileResponse uploadProfilePicture(MultipartFile file);

    /**
     * returns all users. Admin only.
     */
    List<UserProfileResponse> getAllUsers();

    /**
     * soft-deletes a user by id. Admin only.
     */
    void softDeleteUser(UUID userId);

    /**
     * Returns all addresses for the currently authenticated user.
     */
    List<Address> getMyAddresses();

    /**
     * Creates a new address for the current user.
     */
    Address createAddress(AddressRequest request);

    /**
     * Updates an existing address, must belong to the current user.
     */
    Address updateAddress(UUID addressId, AddressRequest request);

    /**
     * Hard deletes an address, must belong to the current user.
     */
    void deleteAddress(UUID addressId);
}
