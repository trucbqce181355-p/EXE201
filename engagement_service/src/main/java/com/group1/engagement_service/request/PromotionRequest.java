package com.group1.engagement_service.request;

import com.group1.engagement_service.entity.PromotionType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionRequest {
    @NotBlank(message = "Promotion name is required")
    private String name;

    private String description;

    @NotNull(message = "Promotion type is required")
    private PromotionType type;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Discount value must be positive")
    private BigDecimal value;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    private Boolean featured;

    // Promotion Rules ---
    @DecimalMin(value = "0.0", message = "Minimum order amount cannot be negative")
    private BigDecimal minOrderAmount;

    @Min(value = 1, message = "Minimum quantity must be at least 1")
    private Integer minQuantity;

    private String applicableProducts;
    private String applicableCategories;

    // Limitations ---
    @DecimalMin(value = "0.0", message = "Max discount amount cannot be negative")
    private BigDecimal maxDiscountAmount;

    @Min(value = 1, message = "Total uses must be at least 1")
    private Integer maxUsesTotal;

    @Min(value = 1, message = "Uses per customer must be at least 1")
    private Integer maxUsesPerCustomer;

    private String targetSegmentIds;
}
