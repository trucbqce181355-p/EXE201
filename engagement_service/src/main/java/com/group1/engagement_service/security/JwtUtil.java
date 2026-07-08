package com.group1.engagement_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private static final int MIN_SECRET_BYTES = 32;

    private final Key key;

    public JwtUtil(@Value("${app.security.jwt.secret}") String jwtSecret) {
        String secret = jwtSecret == null ? "" : jwtSecret.trim();

        byte[] keyBytes = null;

        // Try Base64 decode first (common in many auth services)
        try {
            byte[] decoded = Base64.getDecoder().decode(secret);
            if (decoded.length >= MIN_SECRET_BYTES) {
                keyBytes = decoded;
            }
        } catch (IllegalArgumentException ignored) {
            // not base64, fallback to raw UTF-8 below
        }

        if (keyBytes == null) {
            byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
            if (raw.length >= MIN_SECRET_BYTES) {
                keyBytes = raw;
            }
        }

        if (keyBytes == null) {
            throw new IllegalStateException("JWT secret must be at least 256 bits (32 bytes) either as Base64 or raw text.");
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(120) // tolerate minor clock skew
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long extractUserId(Claims claims) {
        Object value = claims.get("userId");
        if (value == null) {
            return null;
        }
        if (value instanceof Number n) {
            return n.longValue();
        }
        if (value instanceof String s) {
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public String extractEmail(Claims claims) {
        Object value = claims.get("email");
        return value == null ? null : value.toString();
    }

    public String extractFullName(Claims claims) {
        Object value = claims.get("fullName");
        return value == null ? null : value.toString();
    }

    public Collection<? extends GrantedAuthority> extractAuthorities(Claims claims) {
        Object rawAuthorities = claims.get("authorities");

        if (rawAuthorities == null) {
            return List.of();
        }

        List<String> authorities;

        // tránh lỗi cast
        if (rawAuthorities instanceof List<?>) {
            authorities = ((List<?>) rawAuthorities)
                    .stream()
                    .map(Object::toString)
                    .toList();
        } else {
            return List.of();
        }

        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
