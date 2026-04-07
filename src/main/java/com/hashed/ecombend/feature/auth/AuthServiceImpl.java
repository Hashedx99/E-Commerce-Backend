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

