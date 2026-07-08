package com.group1.engagement_service.service;

import com.group1.engagement_service.dto.request.TierBenefitRequest;
import com.group1.engagement_service.dto.response.TierBenefitResponse;
import com.group1.engagement_service.entity.LoyaltyConfig;
import com.group1.engagement_service.entity.Tier;
import com.group1.engagement_service.entity.TierBenefit;
import com.group1.engagement_service.exception.ResourceNotFoundException;
import com.group1.engagement_service.repository.LoyaltyConfigRepository;
import com.group1.engagement_service.repository.TierBenefitRepository;
import com.group1.engagement_service.repository.TierRepository;
import com.group1.engagement_service.util.LoyaltyValidationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TierBenefitService {

    private final TierRepository tierRepository;
    private final TierBenefitRepository tierBenefitRepository;
    private final LoyaltyConfigRepository loyaltyConfigRepository;

    @Transactional
    public TierBenefitResponse createBenefit(Long tierId, TierBenefitRequest request) {
        Tier tier = getTier(tierId);
        String normalizedType = LoyaltyValidationUtils.normalizeBenefitType(request.getType());
        LoyaltyValidationUtils.validateBenefitValue(normalizedType, request.getValue());

        TierBenefit benefit = TierBenefit.builder()
                .tier(tier)
                .type(normalizedType)
                .value(request.getValue())
                .description(LoyaltyValidationUtils.normalizeDescription(request.getDescription()))
                .build();

        TierBenefit savedBenefit = tierBenefitRepository.save(benefit);
        return mapBenefit(savedBenefit, false, tier);
    }

    @Transactional
    public List<TierBenefitResponse> getBenefits(Long tierId) {
        Tier tier = getTier(tierId);
        List<TierBenefitResponse> responses = new ArrayList<>();

        tierBenefitRepository.findByTierIdOrderByIdAsc(tierId)
                .stream()
                .map(benefit -> mapBenefit(benefit, false, tier))
                .forEach(responses::add);

        if (isInheritanceEnabled()) {
            for (Tier lowerTier : tierRepository.findAllByOrderByMinPointsAsc()) {
                if (lowerTier.getId().equals(tier.getId())) {
                    break;
                }
                lowerTier.getBenefits().stream()
                        .map(benefit -> mapBenefit(benefit, true, lowerTier))
                        .forEach(responses::add);
            }
        }

        return responses;
    }

    @Transactional
    public TierBenefitResponse updateBenefit(Long tierId, Long benefitId, TierBenefitRequest request) {
        Tier tier = getTier(tierId);
        TierBenefit benefit = tierBenefitRepository.findByIdAndTierId(benefitId, tierId)
                .orElseThrow(() -> new ResourceNotFoundException("Benefit not found"));

        String normalizedType = LoyaltyValidationUtils.normalizeBenefitType(request.getType());
        LoyaltyValidationUtils.validateBenefitValue(normalizedType, request.getValue());

        benefit.setType(normalizedType);
        benefit.setDescription(LoyaltyValidationUtils.normalizeDescription(request.getDescription()));
        benefit.setValue(request.getValue());

        return mapBenefit(tierBenefitRepository.save(benefit), false, tier);
    }

    @Transactional
    public void deleteBenefit(Long tierId, Long benefitId) {
        Tier tier = getTier(tierId);
        TierBenefit benefit = tierBenefitRepository.findByIdAndTierId(benefitId, tierId)
                .orElseThrow(() -> new ResourceNotFoundException("Benefit not found"));
        tier.removeBenefit(benefit);
        tierRepository.saveAndFlush(tier);
    }

    private Tier getTier(Long tierId) {
        return tierRepository.findById(tierId)
                .orElseThrow(() -> new ResourceNotFoundException("Tier not found"));
    }

    private boolean isInheritanceEnabled() {
        return loyaltyConfigRepository.findById(LoyaltyConfig.SINGLETON_ID)
                .map(LoyaltyConfig::getInheritFromLowerTiers)
                .orElse(Boolean.FALSE);
    }

    private TierBenefitResponse mapBenefit(TierBenefit benefit, boolean inherited, Tier sourceTier) {
        return TierBenefitResponse.builder()
                .id(benefit.getId())
                .type(benefit.getType())
                .value(benefit.getValue())
                .description(benefit.getDescription())
                .inherited(inherited)
                .sourceTierId(sourceTier.getId())
                .sourceTierName(sourceTier.getName())
                .build();
    }
}
