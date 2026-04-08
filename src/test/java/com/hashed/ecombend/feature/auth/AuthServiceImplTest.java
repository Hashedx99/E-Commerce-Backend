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
