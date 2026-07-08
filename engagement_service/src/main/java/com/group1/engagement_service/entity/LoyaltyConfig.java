package com.group1.engagement_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "loyalty_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyConfig {

    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id;

    @Column(name = "points_per_currency", nullable = false, precision = 19, scale = 8)
    private BigDecimal pointsPerCurrency;

    @Column(name = "min_order_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "excluded_categories", length = 1000)
    private String excludedCategories;

    @Column(name = "expiration_months", nullable = false)
    private Integer expirationMonths;

    @Column(name = "evaluation_period_months", nullable = false)
    private Integer evaluationPeriodMonths;

    @Column(name = "inherit_from_lower_tiers", nullable = false)
    private Boolean inheritFromLowerTiers;
}
