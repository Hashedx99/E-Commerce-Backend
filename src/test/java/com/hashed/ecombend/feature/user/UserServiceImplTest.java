package com.hashed.ecombend.feature.user;

import com.hashed.ecombend.common.exception.BusinessException;
import com.hashed.ecombend.common.exception.ResourceNotFoundException;
import com.hashed.ecombend.common.util.SecurityUtil;
import com.hashed.ecombend.feature.storage.StorageService;
import com.hashed.ecombend.feature.user.address.AddressRepository;
import com.hashed.ecombend.feature.user.dto.UpdateProfileRequest;
import com.hashed.ecombend.feature.user.dto.UserProfileResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private StorageService storageService;

    @InjectMocks
    private UserServiceImpl userService;

    private MockedStatic<SecurityUtil> securityUtilMock;
    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        setId(currentUser, UUID.randomUUID());
        currentUser.setName("Alice Johnson");
        currentUser.setEmail("alice@example.com");
        currentUser.setRole(UserRole.ADMIN); // admin for softDelete tests

        securityUtilMock = mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);
    }

    @AfterEach
    void tearDown() {
        securityUtilMock.close();
    }


    @Test
    @DisplayName("getMyProfile: returns DTO populated from current user")
    void getMyProfile_returnsDto() {
        UserProfileResponse result = userService.getMyProfile();

        assertThat(result.getName()).isEqualTo("Alice Johnson");
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("updateMyProfile: new name — updates name only")
    void updateMyProfile_nameOnly_updatesName() {
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setName("Alice Smith");

        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfileResponse result = userService.updateMyProfile(req);

        assertThat(result.getName()).isEqualTo("Alice Smith");
        assertThat(result.getEmail()).isEqualTo("alice@example.com"); // unchanged
    }

    @Test
    @DisplayName("updateMyProfile: email taken by another account — throws BusinessException")
    void updateMyProfile_emailTakenByAnother_throwsException() {
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setEmail("taken@example.com");

        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateMyProfile(req)).isInstanceOf(BusinessException.class).hasMessageContaining("already in use");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateMyProfile: same email as current — no uniqueness check, no update")
    void updateMyProfile_sameEmail_skipsUpdate() {
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setEmail("alice@example.com"); // same as current

        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfileResponse result = userService.updateMyProfile(req);

        // existsByEmail should NOT be called when email hasn't changed
        verify(userRepository, never()).existsByEmail(any());
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("softDeleteUser: success — sets deletedAt on target user")
    void softDeleteUser_success() {
        User target = new User();
        UUID targetId = UUID.randomUUID();
        setId(target, targetId);
        target.setEmail("bob@example.com");

        when(userRepository.findById(targetId)).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenReturn(target);

        userService.softDeleteUser(targetId);

        assertThat(target.isDeleted()).isTrue();
        verify(userRepository).save(target);
    }

    @Test
    @DisplayName("softDeleteUser: admin deletes themselves — throws BusinessException")
    void softDeleteUser_selfDelete_throwsException() {
        // currentUser.getId() == the id we pass in
        UUID selfId = currentUser.getId();

        assertThatThrownBy(() -> userService.softDeleteUser(selfId)).isInstanceOf(BusinessException.class).hasMessageContaining("cannot delete your own account");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("softDeleteUser: user not found — throws ResourceNotFoundException")
    void softDeleteUser_notFound_throwsException() {
        UUID missingId = UUID.randomUUID();
        when(userRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.softDeleteUser(missingId)).isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("User");
    }

    private void setId(Object entity, UUID id) {
        try {
            var f = com.hashed.ecombend.common.entity.BaseEntity.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
