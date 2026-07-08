package com.group1.production_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
public class JwtUtil {

    private static final int MIN_SECRET_BYTES = 32;
    private final Key key;

    public JwtUtil(@Value("${app.security.jwt.secret}") String jwtSecret) {
        String secret = jwtSecret == null ? "" : jwtSecret.trim();
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT secret must be at least 256 bits (32 characters). Current length: " + secretBytes.length
            );
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long extractUserId(Claims claims) {
        Object value = claims.get("userId");
        return value == null ? null : Long.valueOf(value.toString());
    }

    public String extractEmail(Claims claims) {
        Object value = claims.get("email");
        return value == null ? null : value.toString();
    }

    public String extractFullName(Claims claims) {
        Object value = claims.get("fullName");
        return value == null ? null : value.toString();
    }

    public Collection<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        Object value = claims.get("authorities") != null ? claims.get("authorities") : claims.get("roles");
        if (value == null) {
            return List.of();
        }

        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(Object::toString)
                    .map(SimpleGrantedAuthority::new)
                    .toList();
        }

        return Arrays.stream(value.toString().split(","))
                .map(String::trim)
                .filter(authority -> !authority.isEmpty())
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
