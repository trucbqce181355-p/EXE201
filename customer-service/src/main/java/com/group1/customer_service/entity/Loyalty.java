package com.group1.customer_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "loyalty")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loyalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loyaltyId;

    @Column(nullable = false)
    private Integer currentPoints = 0;

    @Column(nullable = false)
    private Integer lifetimePoints = 0;

    @Column(nullable = false)
    private String currentTier = "BRONZE";

    private LocalDateTime enrolledAt;

    private LocalDateTime lastUpdated;

    private LocalDateTime lastEarnedAt;

    @OneToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
}