package com.hashed.ecombend.feature.user;

import com.hashed.ecombend.common.exception.BusinessException;
import com.hashed.ecombend.common.exception.ResourceNotFoundException;
import com.hashed.ecombend.common.util.SecurityUtil;
import com.hashed.ecombend.feature.storage.StorageService;
import com.hashed.ecombend.feature.user.address.Address;
import com.hashed.ecombend.feature.user.address.AddressRepository;
import com.hashed.ecombend.feature.user.address.AddressRequest;
import com.hashed.ecombend.feature.user.dto.UpdateProfileRequest;
import com.hashed.ecombend.feature.user.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final StorageService storageService;

    /**
     * Returns a DTO of the currently authenticated user's profile.
     * Never returns the raw entity always project to UserProfileResponse.
     */
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile() {
        return UserProfileResponse.from(SecurityUtil.getCurrentUser());
    }

    /**
     * Updates name and/or email for the current user.
     * Only non-null, non-blank values are applied (partial update).
     *
     * @throws BusinessException if the requested email belongs to a different account
     */
    @Override
    public UserProfileResponse updateMyProfile(UpdateProfileRequest request) {
        User user = SecurityUtil.getCurrentUser();

        if (StringUtils.hasText(request.getName())) {
            user.setName(request.getName());
        }

        if (StringUtils.hasText(request.getEmail())
                && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("Email is already in use by another account");
            }
            user.setEmail(request.getEmail());
        }

        User saved = userRepository.save(user);
        log.info("Profile updated for user: {}", saved.getEmail());
        return UserProfileResponse.from(saved);
    }

    /**
     * Uploads a profile picture via StorageService and saves the URL on the user.
     * Old pictures are deleted from storage when a new one is uploaded.
     *
     * @param file Image file (jpg/png/webp, max 5MB, enforced by LocalStorageService)
     * @return Updated profile response with the new picture URL
     */
    @Override
    public UserProfileResponse uploadProfilePicture(MultipartFile file) {
        User user = SecurityUtil.getCurrentUser();

        // Delete old picture if one exists
        if (StringUtils.hasText(user.getProfilePictureUrl())) {
            storageService.delete(user.getProfilePictureUrl());
        }

        String url = storageService.store(file, "profiles/" + user.getId());
        user.setProfilePictureUrl(url);
        User saved = userRepository.save(user);
        log.info("Profile picture updated for user: {}", saved.getEmail());
        return UserProfileResponse.from(saved);
    }

    /**
     * Returns all non-deleted users. Admin only enforced by @PreAuthorize in controller.
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserProfileResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Soft-deletes a user by setting deleted_at.
     * The @SQLRestriction on User means this user becomes invisible to all queries.
     * They will not be able to log in MyUserDetails.isEnabled() returns false.
     * Admin only enforced by @PreAuthorize in controller.
     *
     * @throws ResourceNotFoundException if user not found
     * @throws BusinessException         if admin tries to delete themselves
     */
    @Override
    public void softDeleteUser(UUID userId) {
        User currentAdmin = SecurityUtil.getCurrentUser();
        if (currentAdmin.getId().equals(userId)) {
            throw new BusinessException("You cannot delete your own account");
        }

        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        target.softDelete();
        userRepository.save(target);
        log.info("User soft-deleted: {} by admin: {}", target.getEmail(), currentAdmin.getEmail());
    }

    /**
     * Returns all addresses for the current user, default address first.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Address> getMyAddresses() {
        return addressRepository.findByUserIdOrderByDefaultAddressDesc(
                SecurityUtil.getCurrentUser().getId()
        );
    }

    /**
     * Creates a new address for the current user.
     * If isDefault is true, all existing addresses are first set to non-default.
     */
    @Override
    public Address createAddress(AddressRequest request) {
        User user = SecurityUtil.getCurrentUser();

        if (request.isDefaultAddress()) {
            addressRepository.clearDefaultForUser(user.getId());
        }

        Address address = buildAddress(request, user);
        Address saved = addressRepository.save(address);
        log.info("Address created for user: {}", user.getEmail());
        return saved;
    }

    /**
     * Updates an existing address. Verifies ownership before updating.
     *
     * @throws ResourceNotFoundException if address not found or belongs to another user
     */
    @Override
    public Address updateAddress(UUID addressId, AddressRequest request) {
        User user = SecurityUtil.getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        if (request.isDefaultAddress()) {
            addressRepository.clearDefaultForUser(user.getId());
        }

        applyAddressUpdates(address, request);
        return addressRepository.save(address);
    }

    /**
     * Hard deletes an address. Verifies ownership before deleting.
     *
     * @throws ResourceNotFoundException if address not found or belongs to another user
     */
    @Override
    public void deleteAddress(UUID addressId) {
        User user = SecurityUtil.getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        addressRepository.delete(address);
    }

    private Address buildAddress(AddressRequest req, User user) {
        Address a = new Address();
        a.setUser(user);
        applyAddressUpdates(a, req);
        return a;
    }

    private void applyAddressUpdates(Address address, AddressRequest req) {
        address.setLabel(req.getLabel());
        address.setFullName(req.getFullName());
        address.setPhone(req.getPhone());
        address.setLine1(req.getLine1());
        address.setLine2(req.getLine2());
        address.setCity(req.getCity());
        address.setState(req.getState());
        address.setPostalCode(req.getPostalCode());
        address.setCountry(req.getCountry());
        address.setDefaultAddress(req.isDefaultAddress());
    }
}
