package com.group1.engagement_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "loyalty_balances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", unique = true, nullable = false)
    private Long customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id")
    private Tier currentTier;

    @Column(name = "current_points", nullable = false)
    private Integer currentPoints = 0;

    @Column(name = "pending_points", nullable = false)
    private Integer pendingPoints = 0;

    @Column(name = "total_points_earned")
    private Integer totalPointsEarned = 0; // Dùng để tính toán lên hạng
}
