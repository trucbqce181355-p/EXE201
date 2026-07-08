package com.group1.engagement_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // --- ENUM ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromotionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PromotionStatus status = PromotionStatus.DRAFT;

    @Column(nullable = false)
    private BigDecimal value;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    // --- Conditions ---
    @Column(name = "image_url")
    private String imageUrl;

    // --- CÁC ĐIỀU KIỆN ---
    @Column(name = "min_order_amount")
    private BigDecimal minOrderAmount;

    @Column(name = "min_quantity")
    private Integer minQuantity;

    @Column(name = "applicable_products")
    private String applicableProducts;

    @Column(name = "applicable_categories")
    private String applicableCategories;

    // --- Limit ---
    @Column(name = "max_discount_amount")
    private BigDecimal maxDiscountAmount;

    @Column(name = "max_uses_total")
    private Integer maxUsesTotal;

    @Column(name = "max_uses_per_customer")
    private Integer maxUsesPerCustomer;

    @Column(name = "target_segment_ids")
    private String targetSegmentIds;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_segment_mode")
    private PromotionTargetMode targetSegmentMode;

    // --- SOFT DELETE & AUDIT ---
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}