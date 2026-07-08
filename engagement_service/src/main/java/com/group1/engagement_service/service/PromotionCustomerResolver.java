package com.group1.engagement_service.service;

import com.group1.engagement_service.client.CustomerSegmentFeignClient;
import com.group1.engagement_service.security.AuthenticatedUser;
import com.group1.engagement_service.security.JwtUtil;
import feign.FeignException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Map JWT → customer_id (customer-service). Ưu tiên {@link AuthenticatedUser} sau filter.
 * Không có profile khách → {@code resolveEffectiveCustomerId} trả {@code null} nhưng
 * {@link #resolveAuthenticatedUserId} vẫn có thể có userId — catalog dùng để không coi như khách ẩn danh.
 */
@Service
@RequiredArgsConstructor
public class PromotionCustomerResolver {

    private final JwtUtil jwtUtil;
    private final CustomerSegmentFeignClient customerSegmentFeignClient;

    /**
     * User đã xác thực (JWT hợp lệ / SecurityContext) — có thể không có hàng customer ở customer-service.
     */
    public Optional<Long> resolveAuthenticatedUserId(String authorizationHeader) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser au) {
            if (au.getUserId() != null) {
                return Optional.of(au.getUserId());
            }
        }
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return Optional.empty();
        }
        String token = bearerToken(authorizationHeader);
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims claims = jwtUtil.extractAllClaims(token);
            Long userId = jwtUtil.extractUserId(claims);
            return userId != null ? Optional.of(userId) : Optional.empty();
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public Long resolveEffectiveCustomerId(String authorizationHeader) {
        Optional<Long> userIdOpt = resolveAuthenticatedUserId(authorizationHeader);
        if (userIdOpt.isEmpty()) {
            return null;
        }
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        String auth = authorizationHeader.trim();
        try {
            Map<String, Object> body = customerSegmentFeignClient.findCustomerIdByUserId(userIdOpt.get(), auth);
            return mapId(body);
        } catch (FeignException e) {
            return null;
        }
    }

    private static Long mapId(Map<String, Object> body) {
        if (body == null) {
            return null;
        }
        Object id = body.get("id");
        if (id == null) {
            id = body.get("customerId");
        }
        if (id instanceof Number n) {
            return n.longValue();
        }
        return null;
    }

    private static String bearerToken(String authorizationHeader) {
        String v = authorizationHeader.trim();
        if (!v.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        return v.substring(7).trim();
    }
}
