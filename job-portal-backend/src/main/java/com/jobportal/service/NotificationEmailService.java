package com.jobportal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NotificationEmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEmailService.class);

    private final Optional<JavaMailSender> mailSender;

    @Value("${mail.from:no-reply@jobportal.local}")
    private String fromEmail;

    @Value("${app.frontend-base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    public NotificationEmailService(Optional<JavaMailSender> mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String link = frontendBaseUrl + "/verify-email?token=" + token;
        String subject = "Verify your Job Portal email";
        String body = "Please verify your email by visiting: " + link;
        sendEmail(toEmail, subject, body);
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String link = frontendBaseUrl + "/reset-password?token=" + token;
        String subject = "Reset your Job Portal password";
        String body = "Reset your password using this link: " + link;
        sendEmail(toEmail, subject, body);
    }

    private void sendEmail(String toEmail, String subject, String body) {
        if (mailSender.isEmpty()) {
            LOGGER.info("Mail sender not configured. Email fallback -> to: {}, subject: {}, body: {}", toEmail, subject, body);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.get().send(message);
        } catch (Exception ex) {
            LOGGER.warn("Email delivery failed. Content fallback -> to: {}, subject: {}, body: {}", toEmail, subject, body);
        }
    }
}
