package com.group1.engagement_service.service;

import com.group1.engagement_service.dto.request.TierBenefitRequest;
import com.group1.engagement_service.dto.response.TierBenefitResponse;
import com.group1.engagement_service.entity.LoyaltyConfig;
import com.group1.engagement_service.entity.Tier;
import com.group1.engagement_service.entity.TierBenefit;
import com.group1.engagement_service.exception.BadRequestException;
import com.group1.engagement_service.repository.LoyaltyConfigRepository;
import com.group1.engagement_service.repository.TierBenefitRepository;
import com.group1.engagement_service.repository.TierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TierBenefitServiceTest {

    @Mock
    private TierRepository tierRepository;

    @Mock
    private TierBenefitRepository tierBenefitRepository;

    @Mock
    private LoyaltyConfigRepository loyaltyConfigRepository;

    @InjectMocks
    private TierBenefitService tierBenefitService;

    @Test
    void createBenefitRejectsInvalidType() {
        when(tierRepository.findById(1L)).thenReturn(Optional.of(tier(1L, "BRONZE", 0)));

        assertThatThrownBy(() -> tierBenefitService.createBenefit(1L, TierBenefitRequest.builder()
                .type("INVALID")
                .value(BigDecimal.TEN)
                .description("Nope")
                .build()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid benefit type");
    }

    @Test
    void createBenefitRejectsDiscountOutsideAllowedRange() {
        when(tierRepository.findById(1L)).thenReturn(Optional.of(tier(1L, "BRONZE", 0)));

        assertThatThrownBy(() -> tierBenefitService.createBenefit(1L, TierBenefitRequest.builder()
                .type("DISCOUNT")
                .value(new BigDecimal("120"))
                .description("Too much")
                .build()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Discount value must be between 0 and 100");
    }

    @Test
    void getBenefitsIncludesInheritedBenefitsWhenEnabled() {
        Tier bronze = tier(1L, "BRONZE", 0);
        Tier silver = tier(2L, "SILVER", 1000);
        bronze.setBenefits(new ArrayList<>(List.of(TierBenefit.builder()
                .id(10L)
                .tier(bronze)
                .type("FREE_SHIPPING")
                .description("Free ship")
                .build())));
        silver.setBenefits(new ArrayList<>(List.of(TierBenefit.builder()
                .id(11L)
                .tier(silver)
                .type("DISCOUNT")
                .value(new BigDecimal("10"))
                .description("10% off")
                .build())));

        when(tierRepository.findById(2L)).thenReturn(Optional.of(silver));
        when(tierBenefitRepository.findByTierIdOrderByIdAsc(2L)).thenReturn(silver.getBenefits());
        when(tierRepository.findAllByOrderByMinPointsAsc()).thenReturn(List.of(bronze, silver));
        when(loyaltyConfigRepository.findById(LoyaltyConfig.SINGLETON_ID)).thenReturn(Optional.of(LoyaltyConfig.builder()
                .id(LoyaltyConfig.SINGLETON_ID)
                .inheritFromLowerTiers(Boolean.TRUE)
                .build()));

        List<TierBenefitResponse> responses = tierBenefitService.getBenefits(2L);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getInherited()).isFalse();
        assertThat(responses.get(1).getInherited()).isTrue();
        assertThat(responses.get(1).getSourceTierName()).isEqualTo("BRONZE");
    }

    @Test
    void deleteBenefitRemovesBenefitFromTierCollection() {
        Tier bronze = tier(1L, "BRONZE", 0);
        TierBenefit benefit = TierBenefit.builder()
                .id(15L)
                .tier(bronze)
                .type("DISCOUNT")
                .value(new BigDecimal("10"))
                .description("Temporary benefit")
                .build();
        bronze.setBenefits(new ArrayList<>(List.of(benefit)));

        when(tierRepository.findById(1L)).thenReturn(Optional.of(bronze));
        when(tierBenefitRepository.findByIdAndTierId(15L, 1L)).thenReturn(Optional.of(benefit));

        tierBenefitService.deleteBenefit(1L, 15L);

        assertThat(bronze.getBenefits()).doesNotContain(benefit);
        verify(tierRepository).saveAndFlush(bronze);
    }

    private Tier tier(Long id, String name, int minPoints) {
        return Tier.builder()
                .id(id)
                .name(name)
                .minPoints(minPoints)
                .benefits(new ArrayList<>())
                .build();
    }
}
