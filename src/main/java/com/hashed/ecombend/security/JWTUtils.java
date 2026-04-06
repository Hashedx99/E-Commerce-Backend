package com.hashed.ecombend.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class JWTUtils {

    private static final Logger logger = Logger.getLogger(JWTUtils.class.getName());

    @Value("${jwt-secret}")
    private String jwtSecret;

    @Value("${jwt-expiration-ms}")
    private int jwtExpirationMs;

    /**
     * @return SecretKey for signing and verification
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a signed JWT for the given authenticated user.
     * The email address is stored as the token subject.
     *
     * @param userDetails The authenticated principal
     * @return Compact signed JWT string
     */
    public String generateJwtToken(MyUserDetails userDetails) {
        return Jwts.builder().subject(userDetails.getUsername()).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + jwtExpirationMs)).signWith(getSigningKey()).compact();
    }

    /**
     * Extracts the email (subject) from a validated JWT.
     *
     * @param token A JWT string from generateJwtToken()
     * @return The email address stored as the token subject
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload().getSubject();
    }

    /**
     * Validates a JWT token, setting an auth_error request attribute on failure.
     * The JwtRequestFilter reads that attribute to build a 401 response.
     *
     * @param authToken          JWT to validate
     * @param httpServletRequest Current HTTP request (for error attribute)
     * @return true if valid, false if expired/malformed/tampered
     */
    public boolean validateJwtToken(String authToken, HttpServletRequest httpServletRequest) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (SecurityException e) {
            logger.log(Level.SEVERE, "Invalid JWT signature: {0}", e.getMessage());
            httpServletRequest.setAttribute("auth_error", "Invalid JWT signature");
        } catch (MalformedJwtException e) {
            logger.log(Level.SEVERE, "Invalid JWT token: {0}", e.getMessage());
            httpServletRequest.setAttribute("auth_error", "Invalid JWT token");
        } catch (ExpiredJwtException e) {
            logger.log(Level.SEVERE, "JWT token is expired: {0}", e.getMessage());
            httpServletRequest.setAttribute("auth_error", "JWT token is expired");
        } catch (UnsupportedJwtException e) {
            logger.log(Level.SEVERE, "JWT token is unsupported: {0}", e.getMessage());
            httpServletRequest.setAttribute("auth_error", "JWT token is unsupported");
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "JWT claims string is empty: {0}", e.getMessage());
            httpServletRequest.setAttribute("auth_error", "JWT claims string is empty");
        }
        return false;
    }
}
