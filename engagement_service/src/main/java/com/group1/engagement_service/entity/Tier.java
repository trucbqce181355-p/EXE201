package com.group1.engagement_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // BRONZE, SILVER, GOLD, PLATINUM

    @Column(name = "min_points", nullable = false)
    private Integer minPoints;

    @Column(name = "max_points")
    private Integer maxPoints; // Có thể null cho hạng Platinum (10000+)

    // Một hạng có nhiều quyền lợi (tier_benefits)
    @Builder.Default
    @OneToMany(mappedBy = "tier", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<TierBenefit> benefits = new ArrayList<>();

    // Helper method để thêm benefit dễ dàng
    public void addBenefit(TierBenefit benefit) {
        benefits.add(benefit);
        benefit.setTier(this);
    }

    public void removeBenefit(TierBenefit benefit) {
        benefits.remove(benefit);
        benefit.setTier(null);
    }
}