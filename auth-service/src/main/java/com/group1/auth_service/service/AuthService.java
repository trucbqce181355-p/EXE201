package com.group1.auth_service.service;

import com.group1.auth_service.client.CustomerServiceClient;
import com.group1.auth_service.dto.request.ChangePasswordRequest;
import com.group1.auth_service.dto.request.LoginRequest;
import com.group1.auth_service.dto.request.RegisterEmailOtpRequest;
import com.group1.auth_service.dto.request.RegisterRequest;
import com.group1.auth_service.dto.request.VerifyRegisterEmailOtpRequest;
import com.group1.auth_service.dto.response.AuthTokenResponse;
import com.group1.auth_service.entity.Otps;
import com.group1.auth_service.entity.RefreshToken;
import com.group1.auth_service.entity.User;
import com.group1.auth_service.repository.OtpsRepository;
import com.group1.auth_service.repository.UserRepository;
import com.group1.auth_service.security.CustomUserDetailsService;
import com.group1.auth_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final OtpsRepository otpsRepository;
    private final RegisterOtpPersistenceService registerOtpPersistenceService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final CustomerServiceClient customerServiceClient;

    private static final int REGISTER_OTP_LENGTH = 6;
    private static final int REGISTER_OTP_MAX_FAILED_BEFORE_LOCK = 3;
    private static final int REGISTER_OTP_LOCK_MINUTES = 10;
    private static final int REGISTER_OTP_EXPIRE_MINUTES = 5;

    @Transactional
    public void requestRegisterEmailOtp(RegisterEmailOtpRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại");
        }

        LocalDateTime now = LocalDateTime.now();
        Otps otp = otpsRepository.findByEmail(email).orElseGet(Otps::new);

        if (otp.getId() == null) {
            otp.setEmail(email);
        }

        if (otp.getLockUntil() != null && otp.getLockUntil().isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.LOCKED, "Email đang bị khóa, vui lòng thử lại sau");
        }

        // Hết hạn khóa: cho phép gửi OTP mới và reset số lần sai
        if (otp.getLockUntil() != null && !otp.getLockUntil().isAfter(now)) {
            otp.setLockUntil(null);
            otp.setFailedAttemptCount(0);
            otp.setLastFailedAt(null);
        }

        // Không reset failedAttemptCount mỗi lần gửi/resend OTP (trừ khi khóa đã hết ở trên).
        // Trước đây reset ở đây khiến user resend là mất bộ đếm → nhập sai nhiều lần vẫn không khóa.

        String rawOtp = generateNumericOtp(REGISTER_OTP_LENGTH);
        otp.setOtpHash(sha256Hex(rawOtp));
        otp.setExpiresAt(now.plusMinutes(REGISTER_OTP_EXPIRE_MINUTES));
        otp.setVerifiedAt(null);
        otp.setLastSentAt(now);

        otpsRepository.save(otp);
        emailService.sendRegisterEmailOtp(email, rawOtp, otp.getExpiresAt());
    }

    @Transactional
    public void verifyRegisterEmailOtp(VerifyRegisterEmailOtpRequest request) {
        String email = normalizeEmail(request.getEmail());
        String rawOtp = request.getOtp() == null ? "" : request.getOtp().trim();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại");
        }

        Otps otp = otpsRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OTP không tồn tại hoặc đã hết hạn"));

        LocalDateTime now = LocalDateTime.now();
        if (otp.getLockUntil() != null && otp.getLockUntil().isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.LOCKED, "Email đang bị khóa, vui lòng thử lại sau");
        }

        if (otp.getExpiresAt() == null || otp.getExpiresAt().isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.GONE, "OTP đã hết hạn, vui lòng tạo OTP mới");
        }

        String requestHash = sha256Hex(rawOtp);
        boolean match = otp.getOtpHash() != null && otp.getOtpHash().equalsIgnoreCase(requestHash);
        if (!match) {
            int failed = Math.max(0, otp.getFailedAttemptCount()) + 1;
            LocalDateTime lockUntil = null;
            // Sai 3 lần → lần thứ 4 (failed == 4) khóa 10 phút
            if (failed >= REGISTER_OTP_MAX_FAILED_BEFORE_LOCK + 1) {
                lockUntil = now.plusMinutes(REGISTER_OTP_LOCK_MINUTES);
            }

            // Transaction riêng (REQUIRES_NEW) để commit số lần sai / khóa trước khi throw (tránh rollback mất update)
            registerOtpPersistenceService.saveRegisterOtpWrongAttempt(otp.getId(), failed, now, lockUntil);

            if (lockUntil != null) {
                throw new ResponseStatusException(HttpStatus.LOCKED, "Bạn đã nhập sai OTP quá nhiều lần. Email bị khóa 10 phút");
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP không đúng");
        }

        otp.setVerifiedAt(now);
        otp.setFailedAttemptCount(0);
        otp.setLastFailedAt(null);
        otp.setLockUntil(null);
        otpsRepository.save(otp);
    }

    @Transactional
    public AuthTokenResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        userRepository.findByEmail(email)
                .ifPresent(u -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại");
                });

        Otps otp = otpsRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Vui lòng xác thực email bằng OTP trước khi đăng ký"));
        if (otp.getVerifiedAt() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vui lòng xác thực email bằng OTP trước khi đăng ký");
        }

        User user = userService.registerPublic(request);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String accessToken = jwtUtil.generateAccessToken(userDetails, user);
        customerServiceClient.createCustomerForRegisteredUser(accessToken, user.getId());
        String refreshTokenJwt = jwtUtil.generateRefreshToken(userDetails, user);
        refreshTokenService.createAndSaveRefreshToken(user, refreshTokenJwt);

        return AuthTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenJwt)
                .build();
    }

    /** Đăng nhập chỉ bằng email + password. */
    @Transactional
    public AuthTokenResponse login(LoginRequest request) {
        String email = request.getEmail().trim();
        if (email.isBlank()) {
            throw new BadCredentialsException("Email required");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        
        System.out.println(user);
        
        String username = user.getUsername();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword())
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(userDetails, user);
        String refreshTokenJwt = jwtUtil.generateRefreshToken(userDetails, user);
        refreshTokenService.createAndSaveRefreshToken(user, refreshTokenJwt);

        return AuthTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenJwt)
                .build();
    }

    @Transactional
    public AuthTokenResponse refresh(String refreshTokenValue) {
        if (!jwtUtil.isRefreshTokenValid(refreshTokenValue)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }
        RefreshToken stored = refreshTokenService.findByToken(refreshTokenValue);
        if (stored == null || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token not found or expired");
        }
        String username = jwtUtil.extractUsername(refreshTokenValue);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        User user = userRepository.findByUsername(username).orElseThrow();
        Date issuedAt = jwtUtil.extractIssuedAt(refreshTokenValue);
        if (issuedAt == null) {
            refreshTokenService.revokeByToken(refreshTokenValue);
            throw new RuntimeException("Invalid or expired refresh token");
        }
        LocalDateTime issuedAtLocal = issuedAt.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        if (user.getPasswordChangedAt() != null && issuedAtLocal.isBefore(user.getPasswordChangedAt())) {
            refreshTokenService.revokeByToken(refreshTokenValue);
            throw new RuntimeException("Invalid or expired refresh token");
        }

        refreshTokenService.revokeByToken(refreshTokenValue);

        String newAccess = jwtUtil.generateAccessToken(userDetails, user);
        String newRefresh = jwtUtil.generateRefreshToken(userDetails, user);
        refreshTokenService.createAndSaveRefreshToken(user, newRefresh);

        return AuthTokenResponse.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .build();
    }

    public void changePassword(String username, ChangePasswordRequest request) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private static String generateNumericOtp(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(ThreadLocalRandom.current().nextInt(0, 10));
        }
        return sb.toString();
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                String h = Integer.toHexString(b & 0xff);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
