package com.hashed.ecombend.feature.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * response body for a successful POST /auth/login.
 * Returns the JWT and the user's role so the client knows which UI to render.
 */
@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;

    /**
     * The user's role: "ADMIN" or "CUSTOMER".
     */
    private String role;
}
