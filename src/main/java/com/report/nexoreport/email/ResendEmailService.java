package com.report.nexoreport.email;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.report.nexoreport.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Email service implementation using the Resend API (https://resend.com).
 * Uses HTTPS (port 443) — works on Render free tier unlike SMTP (port 587).
 *
 * Activated when app.email.provider=resend (set via RESEND_API_KEY env var).
 * Falls back to SmtpEmailService if this bean is not primary.
 */
@Service
@Primary
public class ResendEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailService.class);

    @Value("${resend.api-key:}")
    private String apiKey;

    @Value("${resend.from-email:onboarding@resend.dev}")
    private String fromEmail;

    @Value("${app.login-url}")
    private String loginUrl;

    @Override
    public void sendInvitationEmail(User user, String rawPassword) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("Resend API key is not configured. Set the RESEND_API_KEY environment variable.");
        }

        Resend resend = new Resend(apiKey);

        String body = buildBody(user, rawPassword);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(user.getEmail())
                .subject("You have been invited to NexaReport")
                .text(body)
                .build();

        try {
            CreateEmailResponse response = resend.emails().send(params);
            log.info("Invitation email sent via Resend to {} — id: {}", user.getEmail(), response.getId());
        } catch (ResendException e) {
            log.error("Resend failed for {}: {}", user.getEmail(), e.getMessage());
            throw new RuntimeException("Failed to send invitation email: " + e.getMessage(), e);
        }
    }

    private String buildBody(User user, String rawPassword) {
        return "Welcome to NexaReport — Issue Reporting System.\n\n"
                + "Your account has been created by an administrator.\n\n"
                + "Email: " + user.getEmail() + "\n"
                + "Temporary Password: " + rawPassword + "\n"
                + "Login URL: " + loginUrl + "\n\n"
                + "Please log in using the temporary password above and change it immediately.\n\n"
                + "If you did not expect this invitation, please ignore this email.";
    }
}
