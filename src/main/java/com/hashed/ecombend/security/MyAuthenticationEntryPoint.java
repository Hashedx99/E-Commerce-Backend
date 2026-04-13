package com.hashed.ecombend.security;

import tools.jackson.databind.ObjectMapper;
import com.hashed.ecombend.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Returns a JSON 401 response when an unauthenticated request hits a protected endpoint.
 * Without this, Spring Security returns an HTML error page not useful for a REST API.
 * <p>
 * The auth_error request attribute is set by JWTUtils.validateJwtToken() with a
 * specific message (e.g. "JWT token is expired") we surface that to the client.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MyAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        String errorMsg = request.getAttribute("auth_error") != null
                ? (String) request.getAttribute("auth_error")
                : authException.getMessage();

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.error(errorMsg));
    }
}
