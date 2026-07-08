package com.group1.engagement_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_usages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "order_id", nullable = false)
    private Long orderId; // Đơn hàng nào xài mã này

    @Column(name = "discount_amount")
    private BigDecimal discountAmount; // Tiền thực tế đã giảm cho đơn này 
    @Column(name = "used_at")
    private LocalDateTime usedAt = LocalDateTime.now();
}
