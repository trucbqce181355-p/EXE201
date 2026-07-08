package com.group1.engagement_service.service;

import com.group1.engagement_service.dto.TierDTO;
import com.group1.engagement_service.dto.TierBenefitDTO;
import com.group1.engagement_service.dto.CustomerTierDTO;
import com.group1.engagement_service.dto.TierHistoryDTO;

import com.group1.engagement_service.entity.LoyaltyBalance;
import com.group1.engagement_service.entity.Tier;
import com.group1.engagement_service.entity.TierBenefit;
import com.group1.engagement_service.entity.TierHistory;

import com.group1.engagement_service.repository.TierRepository;
import com.group1.engagement_service.repository.LoyaltyBalanceRepository;
import com.group1.engagement_service.repository.TierHistoryRepository;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TierService {

    private final TierRepository tierRepository;
    private final LoyaltyBalanceRepository loyaltyBalanceRepository;
    private final TierHistoryRepository tierHistoryRepository; // ✅ thêm

    public TierService(TierRepository tierRepository,
                       LoyaltyBalanceRepository loyaltyBalanceRepository,
                       TierHistoryRepository tierHistoryRepository) {
        this.tierRepository = tierRepository;
        this.loyaltyBalanceRepository = loyaltyBalanceRepository;
        this.tierHistoryRepository = tierHistoryRepository;
    }

    // ✅ AC 35.1
    public List<TierDTO> getAllTiers() {
        List<Tier> tiers = tierRepository.findAll();

        return tiers.stream().map(tier -> {
            List<TierBenefit> tierBenefits =
                    tier.getBenefits() != null ? tier.getBenefits() : new ArrayList<>();

            List<TierBenefitDTO> benefits = tierBenefits.stream()
                    .map(b -> new TierBenefitDTO(
                            b.getDescription(),
                            b.getType(),
                            b.getValue()))
                    .collect(Collectors.toList());

            String iconUrl;
            switch (tier.getName().toLowerCase()) {
                case "silver": iconUrl = "/icons/silver.png"; break;
                case "gold": iconUrl = "/icons/gold.png"; break;
                case "platinum": iconUrl = "/icons/platinum.png"; break;
                default: iconUrl = "/icons/default.png"; break;
            }

            return new TierDTO(
                    tier.getName(),
                    tier.getMinPoints(),
                    tier.getMaxPoints(),
                    iconUrl,
                    benefits
            );
        }).collect(Collectors.toList());
    }

    // ✅ AC 35.2 + 35.3
    public CustomerTierDTO getCustomerTier(Long customerId) {

        LoyaltyBalance balance = loyaltyBalanceRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Loyalty balance not found"));

        int currentPoints = balance.getCurrentPoints();

        List<Tier> tiers = tierRepository.findAll().stream()
                .sorted(Comparator.comparing(Tier::getMinPoints))
                .collect(Collectors.toList());

        Tier currentTier = null;
        Tier nextTier = null;

        for (int i = 0; i < tiers.size(); i++) {
            Tier tier = tiers.get(i);

            if (tier.getMaxPoints() == null || currentPoints <= tier.getMaxPoints()) {
                currentTier = tier;
                if (i + 1 < tiers.size()) nextTier = tiers.get(i + 1);
                break;
            }
        }

        if (currentTier == null) {
            currentTier = tiers.get(tiers.size() - 1);
        }

        // ===== AC 35.3 =====
        int progressPercentage = 100;
        int pointsToNextTier = 0;
        String nextTierName = null;
        boolean isMaxTier = (nextTier == null);

        if (!isMaxTier) {
            int range = nextTier.getMinPoints() - currentTier.getMinPoints();
            int earned = currentPoints - currentTier.getMinPoints();

            progressPercentage = Math.min(100, (int) Math.round(
                    ((double) Math.max(0, earned) / range) * 100));

            pointsToNextTier = Math.max(0, nextTier.getMinPoints() - currentPoints);
            nextTierName = nextTier.getName();
        }

        List<TierBenefitDTO> benefits = currentTier.getBenefits() != null
                ? currentTier.getBenefits().stream()
                .map(b -> new TierBenefitDTO(b.getDescription(), b.getType(), b.getValue()))
                .collect(Collectors.toList())
                : new ArrayList<>();

        String iconUrl;
        switch (currentTier.getName().toLowerCase()) {
            case "silver": iconUrl = "/icons/silver.png"; break;
            case "gold": iconUrl = "/icons/gold.png"; break;
            case "platinum": iconUrl = "/icons/platinum.png"; break;
            default: iconUrl = "/icons/default.png"; break;
        }

        return new CustomerTierDTO(
                currentTier.getName(),
                currentTier.getMinPoints(),
                currentTier.getMaxPoints(),
                iconUrl,
                benefits,
                progressPercentage,
                pointsToNextTier,
                nextTierName,
                isMaxTier
        );
    }

    // ✅ AC 35.5 - Tier History
    
}