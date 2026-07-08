package com.group1.auth_service.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group1.auth_service.entity.RefreshToken;
import com.group1.auth_service.entity.User;
import com.group1.auth_service.repository.RefreshTokenRepository;
import com.group1.auth_service.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Value("${app.security.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Transactional
    public String createAndSaveRefreshToken(User user, String jwtRefreshToken) {
        RefreshToken rt = new RefreshToken();
        rt.setToken(jwtRefreshToken);
        rt.setUser(user);
        rt.setExpiresAt(LocalDateTime.ofInstant(
                Instant.ofEpochMilli(System.currentTimeMillis() + refreshExpirationMs),
                ZoneId.systemDefault()));
        refreshTokenRepository.save(rt);
        return jwtRefreshToken;
    }

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token).orElse(null);
    }

    @Transactional
    public void revokeByToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }

    // Phương thức cũ của bạn
    @Transactional
    public void revokeAllByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    // --- THÊM PHƯƠNG THỨC NÀY ĐỂ FIX LỖI BIÊN DỊCH ---
    @Transactional
    public void revokeByUser(User user) {
        this.revokeAllByUser(user);
    }
    // ------------------------------------------------

    @Transactional
    public void deleteExpired() {
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}