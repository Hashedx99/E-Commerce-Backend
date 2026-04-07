package com.hashed.ecombend.mailing;

import com.hashed.ecombend.feature.user.User;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Email context for account verification emails sent after registration.
 * Template: templates/mailing/email-verification.html
 */
public class AccountVerificationEmailContext extends AbstractEmailContext {

    @Override
    public <T> void init(T ctx) {
        User user = (User) ctx;
        put("name", user.getName());
        setTemplateLocation("mailing/email-verification");
        setSubject("Verify your Ecombend account");
        setTo(user.getEmail());
    }

    /**
     * Builds the full verification URL and injects it into the template.
     *
     * @param baseUrl The application base URL (e.g. http://localhost:8080)
     * @param token   The UUID verification token stored on the User
     */
    public void buildVerificationUrl(String baseUrl, String token) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/auth/verify")
                .queryParam("token", token)
                .toUriString();
        put("verificationURL", url);
        put("token", token);
    }
}
