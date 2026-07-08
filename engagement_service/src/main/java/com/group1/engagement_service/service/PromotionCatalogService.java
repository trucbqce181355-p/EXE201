package com.group1.engagement_service.service;

import com.group1.engagement_service.dto.PromotionPublicResponse;
import com.group1.engagement_service.entity.Promotion;
import com.group1.engagement_service.entity.PromotionStatus;
import com.group1.engagement_service.entity.PromotionType;
import com.group1.engagement_service.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionCatalogService {

    private final PromotionRepository promotionRepository;
    private final PromotionTargetingService promotionTargetingService;
    private final PromotionCustomerResolver promotionCustomerResolver;

    @Transactional(readOnly = true)
    public List<PromotionPublicResponse> getAvailable(
            String categoryCsv,
            PromotionType type,
            String authorizationHeader) {
        Long effectiveCustomerId = promotionCustomerResolver.resolveEffectiveCustomerId(authorizationHeader);

        LocalDateTime now = LocalDateTime.now();
        List<Promotion> rows = promotionRepository.findAvailableActiveInWindow(PromotionStatus.ACTIVE, now, type);
        rows = filterByCategories(rows, parseCategoryTokens(categoryCsv));

        rows = applySegmentVisibilityRules(rows, effectiveCustomerId, authorizationHeader);

        return rows.stream().map(this::toPublic).toList();
    }

    @Transactional(readOnly = true)
    public List<PromotionPublicResponse> getFeatured(
            String categoryCsv,
            PromotionType type,
            String authorizationHeader) {
        Long effectiveCustomerId = promotionCustomerResolver.resolveEffectiveCustomerId(authorizationHeader);

        LocalDateTime now = LocalDateTime.now();
        List<Promotion> rows = promotionRepository.findFeaturedActiveInWindow(PromotionStatus.ACTIVE, now, type);
        rows = filterByCategories(rows, parseCategoryTokens(categoryCsv));

        rows = applySegmentVisibilityRules(rows, effectiveCustomerId, authorizationHeader);

        return rows.stream().map(this::toPublic).toList();
    }

    /**
     * Có customer_id → chỉ KM đã gán segment và khách đủ điều kiện (theo membership) — không đổi (case đúng segment).
     * <p>
     * Không có customer_id (chưa login, hoặc có JWT nhưng chưa có profile khách): trước đây trả gần như toàn bộ KM
     * active → list rất dài; KM có nhắm segment không thể xác định membership nên không nên hiện như catalog đầy đủ.
     * Chỉ giữ KM <strong>chưa gán segment</strong> (public), trùng hành vi cho anonymous và JWT-không-customer.
     */
    private List<Promotion> applySegmentVisibilityRules(
            List<Promotion> rows,
            Long effectiveCustomerId,
            String authorizationHeader) {
        if (effectiveCustomerId != null) {
            return rows.stream()
                    .filter(p -> promotionTargetingService.isEligibleForAuthenticatedAvailableList(
                            p, effectiveCustomerId, authorizationHeader))
                    .toList();
        }
        return rows.stream().toList();
    }

    private static Set<String> parseCategoryTokens(String categoryCsv) {
        if (categoryCsv == null || categoryCsv.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(categoryCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toUpperCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Nếu không truyền category → không lọc.
     * Nếu promotion không set applicable_categories → coi như áp dụng mọi category (vẫn hiển thị).
     * Nếu có set applicable_categories → phải giao với ít nhất một category yêu cầu.
     */
    private static List<Promotion> filterByCategories(List<Promotion> rows, Set<String> wanted) {
        if (wanted.isEmpty()) {
            return rows;
        }
        List<Promotion> out = new ArrayList<>();
        for (Promotion p : rows) {
            String raw = p.getApplicableCategories();
            if (raw == null || raw.isBlank()) {
                out.add(p);
                continue;
            }
            Set<String> promCats = Arrays.stream(raw.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(s -> s.toUpperCase(Locale.ROOT))
                    .collect(Collectors.toSet());
            boolean any = wanted.stream().anyMatch(promCats::contains);
            if (any) {
                out.add(p);
            }
        }
        return out;
    }

    private PromotionPublicResponse toPublic(Promotion p) {
        return PromotionPublicResponse.builder()
                .name(p.getName())
                .description(p.getDescription())
                .type(p.getType())
                .value(p.getValue())
                .conditions(buildConditionsText(p))
                .endDate(p.getEndDate())
                .imageUrl(p.getImageUrl())
                .build();
    }

    private static String buildConditionsText(Promotion p) {
        List<String> parts = new ArrayList<>();
        if (p.getMinOrderAmount() != null) {
            parts.add("Min order amount: " + p.getMinOrderAmount());
        }
        if (p.getMinQuantity() != null) {
            parts.add("Min quantity: " + p.getMinQuantity());
        }
        if (p.getApplicableCategories() != null && !p.getApplicableCategories().isBlank()) {
            parts.add("Categories: " + p.getApplicableCategories());
        }
        if (p.getApplicableProducts() != null && !p.getApplicableProducts().isBlank()) {
            parts.add("Products: " + p.getApplicableProducts());
        }
        if (p.getMaxDiscountAmount() != null) {
            parts.add("Max discount: " + p.getMaxDiscountAmount());
        }
        if (p.getMaxUsesTotal() != null) {
            parts.add("Max uses (campaign): " + p.getMaxUsesTotal());
        }
        if (p.getMaxUsesPerCustomer() != null) {
            parts.add("Max uses per customer: " + p.getMaxUsesPerCustomer());
        }
        return parts.isEmpty() ? null : String.join("; ", parts);
    }
}
