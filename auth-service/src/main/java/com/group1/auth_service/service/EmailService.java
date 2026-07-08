package com.group1.auth_service.service;

import java.time.LocalDateTime;

public interface EmailService {
    void sendPasswordResetToken(String toEmail, String token, LocalDateTime expiresAt);
    void sendEmailChangeOtp(String toEmail, String otp, LocalDateTime expiresAt);
    void sendRegisterEmailOtp(String toEmail, String otp, LocalDateTime expiresAt);
}
