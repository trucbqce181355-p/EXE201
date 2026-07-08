package com.group1.engagement_service.response;

import com.group1.engagement_service.entity.PromotionStatus;
import com.group1.engagement_service.entity.PromotionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionResponse {
    private Long id;
    private String name;
    private String description;

    private PromotionType type;
    private PromotionStatus status;
    private BigDecimal value;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isFeatured;

    // --- Conditions & Limitations ---
    private BigDecimal minOrderAmount;
    private Integer minQuantity;
    private String applicableProducts;
    private String applicableCategories;

    private BigDecimal maxDiscountAmount;
    private Integer maxUsesTotal;
    private Integer maxUsesPerCustomer;
    private String targetSegmentIds;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Usage stats ---
    private Integer totalUses;
}
