package com.group1.customer_service.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final Key key;

    public JwtUtil(@Value("${app.security.jwt.secret}") String jwtSecret) {
        String secret = jwtSecret == null ? "" : jwtSecret.trim();
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
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
        if (value == null) {
            return null;
        }
        return Long.valueOf(value.toString());
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
        Object value = claims.get("authorities") != null
                ? claims.get("authorities")
                : claims.get("roles");

        if (value == null) {
            return List.of();
        }

        List<String> rawValues;
        if (value instanceof Collection<?> col) {
            rawValues = col.stream()
                    .map(Object::toString)
                    .toList();
        } else {
            rawValues = Arrays.stream(value.toString()
                    .replace("[", "")
                    .replace("]", "")
                    .split(","))
                    .map(String::trim)
                    .filter(r -> !r.isEmpty())
                    .toList();
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String raw : rawValues) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            normalized.add(raw);

            // Compatibility: SEGMENT_READ -> SEGMENT:READ
            if (!raw.contains(":") && raw.contains("_") && !raw.startsWith("ROLE_")) {
                int idx = raw.indexOf('_');
                if (idx > 0 && idx < raw.length() - 1) {
                    normalized.add(raw.substring(0, idx) + ":" + raw.substring(idx + 1));
                }
            }

            // Only normalize role-like values, do not force permission to ROLE_*
            if ("ADMIN".equals(raw) || "MANAGER".equals(raw) || "CUSTOMER".equals(raw) || "USER".equals(raw)) {
                normalized.add("ROLE_" + raw);
            }
        }

        return normalized.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
