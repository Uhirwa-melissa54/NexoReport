package com.report.nexoreport.email;

import com.report.nexoreport.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@nexoreport.com}")
    private String fromEmail;

    @Value("${app.login-url}")
    private String loginUrl;

    public SmtpEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendInvitationEmail(User user, String rawPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("You have been invited to the system");
        message.setText(buildBody(user, rawPassword));
        mailSender.send(message);
    }

    private String buildBody(User user, String rawPassword) {
        return "Welcome to the Issue Reporting System.\n\n"
                + "Your account has been created.\n"
                + "Email: " + user.getEmail() + "\n"
                + "Temporary Password: " + rawPassword + "\n"
                + "Login URL: " + loginUrl + "\n\n"
                + "Please log in and update your password.";
    }
}
