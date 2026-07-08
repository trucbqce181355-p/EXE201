package com.group1.customer_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "tier_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tierHistoryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tier fromTier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tier toTier;

    @Enumerated(EnumType.STRING) 
    @Column(nullable = false)
    private TierChangeReason reason;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @PrePersist
    public void prePersist() {
        this.changedAt = LocalDateTime.now();
    }
}