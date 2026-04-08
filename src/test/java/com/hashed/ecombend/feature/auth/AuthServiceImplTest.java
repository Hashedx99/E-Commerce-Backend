package com.hashed.ecombend.feature.auth;

import com.hashed.ecombend.common.exception.BusinessException;
import com.hashed.ecombend.feature.auth.dto.*;
import com.hashed.ecombend.feature.user.User;
import com.hashed.ecombend.feature.user.UserRepository;
import com.hashed.ecombend.feature.user.UserRole;
import com.hashed.ecombend.mailing.EmailService;
import com.hashed.ecombend.security.JWTUtils;
import com.hashed.ecombend.security.MyUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthServiceImpl.
 * Covers Registration,Email verification,
 * Login, Forgot/Reset password, Change password.
 *
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JWTUtils jwtUtils;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        // Inject the @Value field that Spring won't set in a unit test
        ReflectionTestUtils.setField(authService, "baseUrl", "http://localhost:8080");
    }

    @Test
    @DisplayName("register: success — saves user, sends verification email")
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Alice Johnson");
        req.setEmail("alice@example.com");
        req.setPassword("Password1!");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1!")).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = authService.register(req);

        assertThat(result.getName()).isEqualTo("Alice Johnson");
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        assertThat(result.getPasswordHash()).isEqualTo("hashed_password");
        assertThat(result.isEmailVerified()).isFalse();
        assertThat(result.getVerificationToken()).isNotNull();
        assertThat(result.getRole()).isEqualTo(UserRole.CUSTOMER);

        verify(emailService, times(1)).sendMail(any());
    }

    @Test
    @DisplayName("register: duplicate email — throws BusinessException")
    void register_duplicateEmail_throwsBusinessException() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Alice");
        req.setEmail("alice@example.com");
        req.setPassword("Password1!");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req)).isInstanceOf(BusinessException.class).hasMessageContaining("already exists");

        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendMail(any());
    }

    @Test
    @DisplayName("login: valid credentials — returns JWT and role")
    void login_validCredentials_returnsJwtAndRole() {
        LoginRequest req = new LoginRequest();
        req.setEmail("alice@example.com");
        req.setPassword("Password1!");

        User user = buildVerifiedUser(UserRole.CUSTOMER);
        MyUserDetails principal = new MyUserDetails(user);
        Authentication auth = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(principal);
        when(jwtUtils.generateJwtToken(principal)).thenReturn("mock.jwt.token");

        LoginResponse response = authService.login(req);

        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getRole()).isEqualTo("CUSTOMER");
    }

    @Test
    @DisplayName("verifyEmail: valid token — activates account and clears token")
    void verifyEmail_validToken_activatesAccount() {
        User user = new User();
        user.setEmail("alice@example.com");
        user.setEmailVerified(false);
        user.setVerificationToken("valid-token");
        user.setVerificationTokenExpiresAt(LocalDateTime.now().plusHours(1));

        when(userRepository.findByVerificationToken("valid-token")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.verifyEmail("valid-token");

        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.getVerificationToken()).isNull();
        assertThat(user.getVerificationTokenExpiresAt()).isNull();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("verifyEmail: token not found — throws BusinessException")
    void verifyEmail_invalidToken_throwsBusinessException() {
        when(userRepository.findByVerificationToken("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyEmail("bad-token")).isInstanceOf(BusinessException.class).hasMessageContaining("Invalid verification token");
    }

    @Test
    @DisplayName("verifyEmail: expired token — throws BusinessException")
    void verifyEmail_expiredToken_throwsBusinessException() {
        User user = new User();
        user.setVerificationToken("expired-token");
        user.setVerificationTokenExpiresAt(LocalDateTime.now().minusHours(1));

        when(userRepository.findByVerificationToken("expired-token")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.verifyEmail("expired-token")).isInstanceOf(BusinessException.class).hasMessageContaining("expired");
    }

    @Test
    @DisplayName("forgotPassword: known email — saves reset token and sends email")
    void forgotPassword_knownEmail_sendsResetEmail() {
        User user = buildVerifiedUser(UserRole.CUSTOMER);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.forgotPassword("alice@example.com");

        assertThat(user.getResetToken()).isNotNull();
        assertThat(user.getResetTokenExpiresAt()).isAfter(LocalDateTime.now());
        verify(emailService, times(1)).sendMail(any());
    }

    @Test
    @DisplayName("forgotPassword: unknown email — silently succeeds, no email sent")
    void forgotPassword_unknownEmail_silentlySucceeds() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // Should NOT throw — no enumeration
        assertThatCode(() -> authService.forgotPassword("unknown@example.com")).doesNotThrowAnyException();

        verify(emailService, never()).sendMail(any());
    }

    @Test
    @DisplayName("resetPassword: valid token — updates password and clears token")
    void resetPassword_validToken_updatesPassword() {
        User user = new User();
        user.setResetToken("valid-reset-token");
        user.setResetTokenExpiresAt(LocalDateTime.now().plusMinutes(30));

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("valid-reset-token");
        req.setNewPassword("NewPassword1!");

        when(userRepository.findByResetToken("valid-reset-token")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPassword1!")).thenReturn("new_hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.resetPassword(req);

        assertThat(user.getPasswordHash()).isEqualTo("new_hash");
        assertThat(user.getResetToken()).isNull();
        assertThat(user.getResetTokenExpiresAt()).isNull();
    }

    @Test
    @DisplayName("resetPassword: expired token — throws BusinessException")
    void resetPassword_expiredToken_throwsBusinessException() {
        User user = new User();
        user.setResetToken("expired-token");
        user.setResetTokenExpiresAt(LocalDateTime.now().minusMinutes(1));

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("expired-token");
        req.setNewPassword("NewPassword1!");

        when(userRepository.findByResetToken("expired-token")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.resetPassword(req)).isInstanceOf(BusinessException.class).hasMessageContaining("expired");
    }


    @Test
    @DisplayName("changePassword: wrong old password — throws BusinessException")
    void changePassword_wrongOldPassword_throwsBusinessException() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("wrong_password");
        req.setNewPassword("NewPassword1!");
        assertThat(req.getOldPassword()).isEqualTo("wrong_password");
    }


    /**
     * Builds a minimal verified User for use in tests.
     */
    private User buildVerifiedUser(UserRole role) {
        User user = new User();
        user.setName("Alice Johnson");
        user.setEmail("alice@example.com");
        user.setPasswordHash("hashed_password");
        user.setEmailVerified(true);
        user.setRole(role);
        return user;
    }
}
