package com.group1.engagement_service.service;

import com.group1.engagement_service.dto.response.PromotionAnalyticsResponse;
import com.group1.engagement_service.entity.Promotion;
import com.group1.engagement_service.repository.CouponUsageRepository;
import com.group1.engagement_service.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionAnalyticsService {

    private final CouponUsageRepository couponUsageRepo;
    private final PromotionRepository promotionRepo;

    public PromotionAnalyticsResponse getPromotionAnalytics(Long promotionId) {
        Promotion promotion = promotionRepo.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        BigDecimal totalDiscount = couponUsageRepo.totalDiscountByPromotion(promotionId);
        long totalUses = couponUsageRepo.countByCoupon_Promotion_Id(promotionId);
        long uniqueCustomers = couponUsageRepo.countUniqueCustomers(promotionId);

        // Lấy type String để so sánh (tránh lỗi Enum mapping nếu chưa sửa Enum)
        String promoType = promotion.getType().toString();
        BigDecimal totalRevenue;

        switch (promoType) {
            case "PERCENTAGE":
            case "PERCENTAGE_DISCOUNT":
                totalRevenue = totalDiscount.multiply(BigDecimal.valueOf(10));
                break;
            case "FIXED_AMOUNT":
            case "FIXED_DISCOUNT":
                totalRevenue = totalDiscount.multiply(BigDecimal.valueOf(5));
                break;
            default:
                totalRevenue = totalDiscount.multiply(BigDecimal.valueOf(5));
                break;
        }

        double roi = 0;
        if (totalDiscount.compareTo(BigDecimal.ZERO) > 0) {
            roi = totalRevenue.subtract(totalDiscount)
                    .divide(totalDiscount, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        }

        double conversionRate = 0;
        if (promotion.getMaxUsesTotal() != null && promotion.getMaxUsesTotal() > 0) {
            conversionRate = ((double) totalUses / promotion.getMaxUsesTotal()) * 100;
        }

        return PromotionAnalyticsResponse.builder()
                .promotionId(promotionId)
                .totalUses(totalUses)
                .totalDiscountGiven(totalDiscount)
                .totalRevenueGenerated(totalRevenue)
                .conversionRate(conversionRate)
                .roi(roi)
                .customerMetrics(PromotionAnalyticsResponse.CustomerMetrics.builder()
                        .uniqueCustomers(uniqueCustomers)
                        .build())
                .build();
    }

    public List<Map<String, Object>> getUsageHistory(Long promotionId) {
        try {
            List<Object[]> rows = couponUsageRepo.getUsageHistoryData(promotionId);
            if (rows == null)
                return new ArrayList<>();

            return rows.stream().map(row -> {
                Map<String, Object> m = new HashMap<>();
                m.put("date", row[0] != null ? row[0].toString() : "");

                // SỬA DÒNG NÀY: Đổi "usageCount" thành "count"
                m.put("count", row[1] != null ? row[1] : 0);

                return m;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error processing usage history: " + e.getMessage());
        }
    }

    /**
     * AC 30.4: Sửa phần .map() không dùng Method Reference
     */
    public List<PromotionAnalyticsResponse> comparePromotions(List<Long> ids) {
        return ids.stream()
                .map(id -> this.getPromotionAnalytics(id)) // Dùng Lambda thay cho this::
                .collect(Collectors.toList());
    }
}