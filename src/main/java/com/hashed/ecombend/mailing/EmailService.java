package com.hashed.ecombend.mailing;

/**
 * Contract for sending transactional emails.
 * DefaultEmailService implements this using Spring Mail + Thymeleaf templates.
 */
public interface EmailService {
    /**
     * Sends an email using the context's configured template and recipient.
     *
     * @param email An AbstractEmailContext subclass (verification, reset, etc.)
     */
    void sendMail(AbstractEmailContext email);
}
