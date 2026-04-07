package com.hashed.ecombend.feature.auth;

import com.hashed.ecombend.common.response.ApiResponse;
import com.hashed.ecombend.feature.auth.dto.*;
import com.hashed.ecombend.feature.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 *
 * <p>
 * All endpoints here are PUBLIC — no JWT required.
 * Routes are declared in SecurityConfiguration under .requestMatchers("/auth/**").permitAll()
 * </p>
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, and password management")
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new CUSTOMER account and sends a verification email.
     * The account cannot log in until the email link is clicked.
     *
     * @param request Name, email, password
     * @return 201 Created with the saved user (no JWT yet)
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new customer account")
    public ApiResponse<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ApiResponse.ok(
                "Registration successful. Please check your email to verify your account.",
                user
        );
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param request Email and password
     * @return JWT token + role string
     */
    @PostMapping("/login")
    @Operation(summary = "Login and receive a JWT token")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.ok("Login successful", response);
    }

    /**
     * Verifies an email address via the token link sent during registration.
     *
     * @param token UUID token from the verification email query parameter
     * @return 200 with confirmation message
     */
    @GetMapping("/verify")
    @Operation(summary = "Verify email address via token link")
    public ApiResponse<Void> verify(@RequestParam String token) {
        authService.verifyEmail(token);
        return ApiResponse.ok("Email verified successfully. You can now log in.");
    }

    /**
     * Requests a password reset email. Always returns 200 regardless of
     * whether the email exists — prevents email enumeration.
     *
     * @param request Email address
     * @return Always 200
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset email")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ApiResponse.ok(
                "If an account with that email exists, a reset link has been sent."
        );
    }

    /**
     * Resets a password using the token from the reset email.
     *
     * @param request Token + new password
     * @return 200 on success
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using the emailed token")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.ok("Password reset successfully. You can now log in.");
    }
}
