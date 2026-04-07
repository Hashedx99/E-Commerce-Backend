package com.hashed.ecombend.feature.auth;

import com.hashed.ecombend.feature.auth.dto.*;
import com.hashed.ecombend.feature.user.User;

public interface AuthService {

    /**
     * registers a new CUSTOMER account and sends a verification email.
     * The account cannot log in until the email link is clicked.
     *
     * @param request Name, email, password
     * @return The saved (unverified) User entity
     */
    User register(RegisterRequest request);

    /**
     * authenticates credentials and returns a JWT.
     * Fails with 401 if credentials are wrong or email is not verified.
     *
     * @param request Email and password
     * @return JWT token + role
     */
    LoginResponse login(LoginRequest request);

    /**
     * activates an account via the token link emailed during registration.
     *
     * @param token UUID token from the verification email link
     */
    void verifyEmail(String token);

    /**
     * sends a password reset email.
     * Always returns success
     *
     * @param email The account email to send a reset link to
     */
    void forgotPassword(String email);

    /**
     * completes the password reset using the token from the email.
     *
     * @param request Token + new password
     */
    void resetPassword(ResetPasswordRequest request);

    /**
     * changes the password for the currently authenticated user.
     * Requires the current password to prevent unauthorized changes.
     *
     * @param request Old password + new password
     */
    void changePassword(ChangePasswordRequest request);
}
