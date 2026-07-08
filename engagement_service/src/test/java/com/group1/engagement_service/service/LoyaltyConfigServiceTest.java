package com.group1.engagement_service.service;

import com.group1.engagement_service.dto.request.TierBenefitRequest;
import com.group1.engagement_service.dto.request.TierConfigUpdateRequest;
import com.group1.engagement_service.dto.request.TierDefinitionRequest;
import com.group1.engagement_service.dto.response.LoyaltyConfigResponse;
import com.group1.engagement_service.entity.LoyaltyConfig;
import com.group1.engagement_service.entity.Tier;
import com.group1.engagement_service.exception.BadRequestException;
import com.group1.engagement_service.repository.LoyaltyBalanceRepository;
import com.group1.engagement_service.repository.LoyaltyConfigRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoyaltyConfigServiceTest {

    @Mock
    private LoyaltyConfigRepository loyaltyConfigRepository;

    @Mock
    private TierRepository tierRepository;

    @Mock
    private LoyaltyBalanceRepository loyaltyBalanceRepository;

    @InjectMocks
    private LoyaltyConfigService loyaltyConfigService;

    @Test
    void getConfigCreatesDefaultsWhenDatabaseIsEmpty() {
        when(loyaltyConfigRepository.findById(LoyaltyConfig.SINGLETON_ID)).thenReturn(Optional.empty());
        when(loyaltyConfigRepository.save(any(LoyaltyConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tierRepository.findAllByOrderByMinPointsAsc()).thenReturn(List.of());
        when(tierRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoyaltyConfigResponse response = loyaltyConfigService.getConfig();

        assertThat(response.getPointsPerCurrency()).isEqualByComparingTo(new BigDecimal("0.0001"));
        assertThat(response.getExpirationMonths()).isEqualTo(12);
        assertThat(response.getEvaluationPeriodMonths()).isEqualTo(12);
        assertThat(response.getTiers()).hasSize(4);
        assertThat(response.getTiers()).extracting("name")
                .containsExactly("BRONZE", "SILVER", "GOLD", "PLATINUM");
    }

    @Test
    void updateTierConfigRejectsNonContinuousRanges() {
        when(loyaltyConfigRepository.findById(LoyaltyConfig.SINGLETON_ID)).thenReturn(Optional.of(defaultConfig()));
        when(tierRepository.findAllByOrderByMinPointsAsc()).thenReturn(defaultTiers());

        TierConfigUpdateRequest request = TierConfigUpdateRequest.builder()
                .tiers(List.of(
                        TierDefinitionRequest.builder().name("BRONZE").minPoints(0).maxPoints(999).build(),
                        TierDefinitionRequest.builder().name("SILVER").minPoints(1200).maxPoints(4999).build()
                ))
                .build();

        assertThatThrownBy(() -> loyaltyConfigService.updateTierConfig(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Tier ranges must be continuous and non-overlapping");

        verify(tierRepository, never()).saveAll(any());
    }

    @Test
    void updateTierConfigUpdatesInheritanceAndBenefits() {
        LoyaltyConfig config = defaultConfig();
        List<Tier> existingTiers = defaultTiers();

        when(loyaltyConfigRepository.findById(LoyaltyConfig.SINGLETON_ID)).thenReturn(Optional.of(config));
        when(loyaltyConfigRepository.save(any(LoyaltyConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tierRepository.findAllByOrderByMinPointsAsc()).thenReturn(existingTiers);
        when(tierRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TierConfigUpdateRequest request = TierConfigUpdateRequest.builder()
                .inheritFromLowerTiers(Boolean.TRUE)
                .tiers(List.of(
                        TierDefinitionRequest.builder()
                                .id(1L)
                                .name("BRONZE")
                                .minPoints(0)
                                .maxPoints(999)
                                .benefits(List.of(TierBenefitRequest.builder()
                                        .type("DISCOUNT")
                                        .value(new BigDecimal("5"))
                                        .description("5% off")
                                        .build()))
                                .build(),
                        TierDefinitionRequest.builder().id(2L).name("SILVER").minPoints(1000).maxPoints(4999).build(),
                        TierDefinitionRequest.builder().id(3L).name("GOLD").minPoints(5000).maxPoints(9999).build(),
                        TierDefinitionRequest.builder().id(4L).name("PLATINUM").minPoints(10000).maxPoints(null).build()
                ))
                .build();

        LoyaltyConfigResponse response = loyaltyConfigService.updateTierConfig(request);

        assertThat(response.getInheritFromLowerTiers()).isTrue();
        assertThat(response.getTiers().get(0).getBenefits()).hasSize(1);
        assertThat(response.getTiers().get(0).getBenefits().get(0).getType()).isEqualTo("DISCOUNT");
        assertThat(config.getInheritFromLowerTiers()).isTrue();
    }

    private LoyaltyConfig defaultConfig() {
        return LoyaltyConfig.builder()
                .id(LoyaltyConfig.SINGLETON_ID)
                .pointsPerCurrency(new BigDecimal("0.0001"))
                .minOrderAmount(BigDecimal.ZERO)
                .excludedCategories("")
                .expirationMonths(12)
                .evaluationPeriodMonths(12)
                .inheritFromLowerTiers(Boolean.FALSE)
                .build();
    }

    private List<Tier> defaultTiers() {
        return List.of(
                Tier.builder().id(1L).name("BRONZE").minPoints(0).maxPoints(999).benefits(new ArrayList<>()).build(),
                Tier.builder().id(2L).name("SILVER").minPoints(1000).maxPoints(4999).benefits(new ArrayList<>()).build(),
                Tier.builder().id(3L).name("GOLD").minPoints(5000).maxPoints(9999).benefits(new ArrayList<>()).build(),
                Tier.builder().id(4L).name("PLATINUM").minPoints(10000).maxPoints(null).benefits(new ArrayList<>()).build()
        );
    }
}
