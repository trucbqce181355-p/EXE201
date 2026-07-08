package com.group1.auth_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.reset-password.base-url:http://localhost:5173/reset-password}")
    private String resetPasswordBaseUrl;

    @Value("${spring.mail.username:no-reply@localhost}")
    private String senderEmail;

    @Override
    public void sendPasswordResetToken(String toEmail, String token, LocalDateTime expiresAt) {
        String resetLink = UriComponentsBuilder.fromUriString(resetPasswordBaseUrl)
                .queryParam("token", token)
                .build()
                .toUriString();

        String content = """
                We received a password reset request for your account.
                Open the link below to choose a new password:
                %s

                This link expires at: %s

                If you did not request this, you can ignore this email.
                """.formatted(resetLink, expiresAt);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("BeAn - Be Every Angle Nearby - Password reset");
        message.setText(content);
        if (senderEmail != null && !senderEmail.isBlank()) {
            message.setFrom(senderEmail);
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Mail sender is not configured. Token for {}: {}", toEmail, token);
            return;
        }

        try {
            mailSender.send(message);
        } catch (Exception ex) {
            // Keep API behavior stable in local/dev if SMTP is not configured.
            log.warn("Failed to send reset email to {}. Token for testing: {}", toEmail, token);
            return;
        }

        log.info("Password reset instructions sent to {}", toEmail);
    }

    @Override
    public void sendEmailChangeOtp(String toEmail, String otp, LocalDateTime expiresAt) {
        String content = """
                We received a request to change your account email.
                OTP code: %s
                Expires at: %s
                """.formatted(otp, expiresAt);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("BeAn - Be Every Angle Nearby - Confirm your new email");
        message.setText(content);
        if (senderEmail != null && !senderEmail.isBlank()) {
            message.setFrom(senderEmail);
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Mail sender is not configured. Email change OTP for {}: {}", toEmail, otp);
            return;
        }

        try {
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Failed to send email-change OTP to {}. OTP for testing: {}", toEmail, otp);
            return;
        }

        log.info("Email-change OTP sent to {}", toEmail);
    }

    @Override
    public void sendRegisterEmailOtp(String toEmail, String otp, LocalDateTime expiresAt) {
        String content = """
                We received a request to verify your email for registration.
                OTP code: %s
                Expires at: %s
                """.formatted(otp, expiresAt);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("BeAn - Be Every Angle Nearby - Verify your email");
        message.setText(content);
        if (senderEmail != null && !senderEmail.isBlank()) {
            message.setFrom(senderEmail);
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Mail sender is not configured. Register OTP for {}: {}", toEmail, otp);
            return;
        }

        try {
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Failed to send register OTP to {}. OTP for testing: {}", toEmail, otp);
            return;
        }

        log.info("Register email OTP sent to {}", toEmail);
    }
}
