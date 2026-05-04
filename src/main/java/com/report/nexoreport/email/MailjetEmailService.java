package com.report.nexoreport.email;

import com.report.nexoreport.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

/**
 * Email service using Mailjet Send API v3.1.
 * Uses HTTPS (port 443) — works on Render free tier.
 * Free tier: 200 emails/day, no domain verification required.
 *
 * Sign up at https://mailjet.com — get API Key + Secret Key from
 * Account Settings → REST API → API Key Management.
 */
@Service
@Primary
public class MailjetEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(MailjetEmailService.class);
    private static final String MAILJET_API_URL = "https://api.mailjet.com/v3.1/send";

    @Value("${mailjet.api-key:}")
    private String apiKey;

    @Value("${mailjet.secret-key:}")
    private String secretKey;

    @Value("${mailjet.from-email:}")
    private String fromEmail;

    @Value("${mailjet.from-name:NexaReport}")
    private String fromName;

    @Value("${app.login-url}")
    private String loginUrl;

    @Override
    public void sendInvitationEmail(User user, String rawPassword) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("Mailjet API key not configured. Set mailjet.api-key in application.properties.");
        }
        if (secretKey == null || secretKey.isBlank()) {
            throw new RuntimeException("Mailjet secret key not configured. Set mailjet.secret-key in application.properties.");
        }
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new RuntimeException("Mailjet from-email not configured. Set mailjet.from-email in application.properties.");
        }

        String credentials = Base64.getEncoder()
                .encodeToString((apiKey + ":" + secretKey).getBytes());

        String textContent = buildBody(user, rawPassword);
        String jsonBody = buildJsonBody(user.getEmail(), textContent);

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(MAILJET_API_URL))
                    .header("Authorization", "Basic " + credentials)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Invitation email sent via Mailjet to {}", user.getEmail());
            } else {
                log.error("Mailjet API error {}: {}", response.statusCode(), response.body());
                throw new RuntimeException("Mailjet returned " + response.statusCode() + ": " + response.body());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to send email via Mailjet to {}: {}", user.getEmail(), e.getMessage());
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

    private String buildJsonBody(String toEmail, String textContent) {
        String escaped = textContent
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        return "{"
                + "\"Messages\":["
                + "{"
                + "\"From\":{\"Email\":\"" + fromEmail + "\",\"Name\":\"" + fromName + "\"},"
                + "\"To\":[{\"Email\":\"" + toEmail + "\"}],"
                + "\"Subject\":\"You have been invited to NexaReport\","
                + "\"TextPart\":\"" + escaped + "\""
                + "}"
                + "]"
                + "}";
    }
}
