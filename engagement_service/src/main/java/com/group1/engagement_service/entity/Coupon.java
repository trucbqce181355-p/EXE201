package com.group1.engagement_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity

@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String code; 

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Column(name = "max_uses")
    private Integer maxUses; 

    @Column(name = "times_used", nullable = false)
    private Integer timesUsed = 0;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CouponStatus status = CouponStatus.ACTIVE;
}
