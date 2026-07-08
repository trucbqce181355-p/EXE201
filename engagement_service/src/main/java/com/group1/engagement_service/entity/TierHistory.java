package com.group1.engagement_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tier_history")
public class TierHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;

    private String oldTier;
    private String newTier;

    private LocalDateTime changedAt;
    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public String getOldTier() { return oldTier; }
    public String getNewTier() { return newTier; }
    public LocalDateTime getChangedAt() { return changedAt; }

    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public void setOldTier(String oldTier) { this.oldTier = oldTier; }
    public void setNewTier(String newTier) { this.newTier = newTier; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}