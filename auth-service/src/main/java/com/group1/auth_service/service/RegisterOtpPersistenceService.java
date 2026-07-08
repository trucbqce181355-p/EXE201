package com.group1.auth_service.service;

import com.group1.auth_service.entity.Otps;
import com.group1.auth_service.repository.OtpsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Lưu trạng thái OTP trong transaction riêng (REQUIRES_NEW) để khi {@code verifyRegisterEmailOtp}
 * throw {@link org.springframework.web.server.ResponseStatusException} thì số lần sai / khóa vẫn được commit.
 */
@Service
@RequiredArgsConstructor
public class RegisterOtpPersistenceService {

    private final OtpsRepository otpsRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveRegisterOtpWrongAttempt(Long otpId, int failedAttemptCount, LocalDateTime lastFailedAt,
                                            LocalDateTime lockUntil) {
        Otps otp = otpsRepository.findById(otpId)
                .orElseThrow(() -> new IllegalStateException("Otps not found: " + otpId));
        otp.setFailedAttemptCount(failedAttemptCount);
        otp.setLastFailedAt(lastFailedAt);
        otp.setLockUntil(lockUntil);
        otpsRepository.save(otp);
    }
}
