package com.hashed.ecombend.feature.auth;

import com.hashed.ecombend.common.exception.BusinessException;
import com.hashed.ecombend.common.util.SecurityUtil;
import com.hashed.ecombend.feature.auth.dto.*;
import com.hashed.ecombend.feature.user.User;
import com.hashed.ecombend.feature.user.UserRepository;
import com.hashed.ecombend.mailing.AccountPasswordResetEmailContext;
import com.hashed.ecombend.mailing.AccountVerificationEmailContext;
import com.hashed.ecombend.mailing.EmailService;
import com.hashed.ecombend.security.JWTUtils;
import com.hashed.ecombend.security.MyUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Lazy)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final @Lazy PasswordEncoder passwordEncoder;
    private final @Lazy AuthenticationManager authenticationManager;
    private final JWTUtils jwtUtils;
    private final EmailService emailService;

    @Value("${site.base.url}")
    private String baseUrl;

    /**
     * Registers a new CUSTOMER, hashes the password, generates a verification token,
     * and sends the verification email. The account is NOT usable until verified.
     *
     * @throws BusinessException if the email is already registered
     */
    @Override
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("An account with this email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmailVerified(false);

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setVerificationTokenExpiresAt(LocalDateTime.now().plusHours(24));

        User saved = userRepository.save(user);
        log.info("Registered new user: {}", saved.getEmail());

        sendVerificationEmail(saved, token);
        return saved;
    }


    /**
     * Authenticates credentials and returns a JWT on success.
     * Any failure throws an AuthenticationException → MyAuthenticationEntryPoint → 401.
     */
    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
        MyUserDetails principal = (MyUserDetails) auth.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(principal);

        log.info("User logged in: {}", principal.getUsername());
        return new LoginResponse(jwt, principal.getUser().getRole().name());
    }

    /**
     * Activates an account via the token link. Validates expiry before activating
     *
     * @throws BusinessException if token is invalid or expired
     */
    @Override
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new BusinessException("Invalid verification token"));

        if (user.getVerificationTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(
                    "Verification link has expired. Please register again or request a new link.");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiresAt(null);
        userRepository.save(user);
        log.info("Email verified for user: {}", user.getEmail());
    }

    /**
     * Sends a reset link if the email exists. Always returns 200 regardless.
     */
    @Override
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiresAt(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
            sendPasswordResetEmail(user, token);
            log.info("Password reset email sent to: {}", email);
        });
    }

    /**
     * Completes a password reset. Validates token expiry before updating.
     *
     * @throws BusinessException if token is missing, invalid, or expired
     */
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new BusinessException(
                        "Invalid or expired reset token. Please request a new one."));

        if (user.getResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(
                    "Reset link has expired. Please request a new password reset.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiresAt(null);
        userRepository.save(user);
        log.info("Password reset for user: {}", user.getEmail());
    }

    /**
     * Changes password for the currently authenticated user.
     * Old password must match before allowing the update.
     *
     * @throws BusinessException if oldPassword doesn't match the stored hash
     */
    @Override
    public void changePassword(ChangePasswordRequest request) {
        User user = SecurityUtil.getCurrentUser();

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getEmail());
    }


    /**
     * Sends the email verification email using the Thymeleaf template.
