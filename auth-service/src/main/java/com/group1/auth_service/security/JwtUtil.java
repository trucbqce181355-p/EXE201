package com.group1.auth_service.security;

import com.group1.auth_service.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private final Key key;
    private final long expirationMs;
    private final long refreshExpirationMs;

    private static final int MIN_SECRET_BYTES = 32; // 256 bits for HS256

    public JwtUtil(
            @Value("${app.security.jwt.secret}") String jwtSecret,
            @Value("${app.security.jwt.expiration-ms}") long expirationMs,
            @Value("${app.security.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        String secret = (jwtSecret != null) ? jwtSecret.trim() : "";
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT secret must be at least 256 bits (32 characters). Current length: " + secretBytes.length);
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateToken(UserDetails userDetails) {
        return generateAccessToken(userDetails);
    }

    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(userDetails.getUsername(), userDetails.getAuthorities(), expirationMs, null);
    }

    public String generateAccessToken(UserDetails userDetails, User user) {
        return buildToken(userDetails.getUsername(), userDetails.getAuthorities(), expirationMs, user);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(userDetails.getUsername(), null, refreshExpirationMs, null);
    }

    public String generateRefreshToken(UserDetails userDetails, User user) {
        return buildToken(userDetails.getUsername(), null, refreshExpirationMs, user);
    }

    private String buildToken(String subject, java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities, long validityMs, User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityMs);

        var builder = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry);
        List<String> authorityList;
        if (authorities != null && !authorities.isEmpty()) {
            authorityList = user.getRoles().stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .map(p -> p.getResource() + ":" + p.getAction())
                    .distinct()
                    .toList();
            builder.claim("authorities", authorityList);
        }

        if (user != null) {
            builder.claim("userId", user.getId());
            if (user.getEmail() != null) builder.claim("email", user.getEmail());
            if (user.getFullName() != null) builder.claim("fullName", user.getFullName());
        }

        return builder.signWith(key, SignatureAlgorithm.HS256).compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Object v = extractAllClaims(token).get("userId");
        if (v == null) return null;
        if (v instanceof Integer) return ((Integer) v).longValue();
        if (v instanceof Long) return (Long) v;
        return null;
    }

    public String extractEmail(String token) {
        Object v = extractAllClaims(token).get("email");
        return v != null ? v.toString() : null;
    }

    public String extractFullName(String token) {
        Object v = extractAllClaims(token).get("fullName");
        return v != null ? v.toString() : null;
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isRefreshTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public Date extractIssuedAt(String token) {
        return extractAllClaims(token).getIssuedAt();
    }

    public long getRemainingTime(String token) {
        Date expiration = extractExpiration(token);
        return expiration.getTime() - System.currentTimeMillis();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}