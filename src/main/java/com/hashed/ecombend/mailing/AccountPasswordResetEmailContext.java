package com.hashed.ecombend.mailing;

import com.hashed.ecombend.feature.user.User;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Email context for password reset emails.
 * Template: templates/mailing/password-reset.html
 */
public class AccountPasswordResetEmailContext extends AbstractEmailContext {

    @Override
    public <T> void init(T ctx) {
        User user = (User) ctx;
        put("name", user.getName());
        setTemplateLocation("mailing/password-reset");
        setSubject("Reset your Ecombend password");
        setTo(user.getEmail());
    }

    /**
     * Builds the full reset URL and injects it into the template.
     *
     * @param baseUrl The application base URL
     * @param token   The UUID reset token stored on the User
     */
    public void buildResetUrl(String baseUrl, String token) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/auth/reset-password")
                .queryParam("token", token)
                .toUriString();
        put("resetURL", url);
        put("token", token);
    }
}
