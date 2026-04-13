package com.hashed.ecombend.mailing;

import com.hashed.ecombend.common.exception.EmailDeliveryException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

/**
 * Sends transactional emails using Spring Mail and Thymeleaf HTML templates.
 * Templates live in: src/main/resources/templates/mailing/
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultEmailService implements EmailService {

    @Value("${spring.mail.from}")
    private String fromEmail;

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    /**
     * Renders the Thymeleaf template from the email context and sends it.
     *
     * @param email An email context with recipient, subject, template, and variables
     * @throws RuntimeException if sending fails
     */
    @Override
    public void sendMail(AbstractEmailContext email) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            Context ctx = new Context();
            ctx.setVariables(email.getContext());
            String htmlContent = templateEngine.process(email.getTemplateLocation(), ctx);

            helper.setTo(email.getTo());
            helper.setSubject(email.getSubject());
            helper.setFrom(fromEmail);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent to: {}", email.getTo());
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", email.getTo(), e.getMessage());
            throw new EmailDeliveryException("Failed to send email", e);
        }
    }
}
