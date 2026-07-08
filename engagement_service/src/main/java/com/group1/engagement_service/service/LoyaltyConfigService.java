package com.group1.engagement_service.service;

import com.group1.engagement_service.dto.request.ExpirationConfigRequest;
import com.group1.engagement_service.dto.request.PointConfigRequest;
import com.group1.engagement_service.dto.request.TierBenefitRequest;
import com.group1.engagement_service.dto.request.TierConfigUpdateRequest;
import com.group1.engagement_service.dto.request.TierDefinitionRequest;
import com.group1.engagement_service.dto.response.LoyaltyConfigResponse;
import com.group1.engagement_service.dto.response.TierBenefitResponse;
import com.group1.engagement_service.dto.response.TierConfigResponse;
import com.group1.engagement_service.entity.LoyaltyConfig;
import com.group1.engagement_service.entity.Tier;
import com.group1.engagement_service.entity.TierBenefit;
import com.group1.engagement_service.exception.BadRequestException;
import com.group1.engagement_service.exception.ConflictException;
import com.group1.engagement_service.exception.ResourceNotFoundException;
import com.group1.engagement_service.repository.LoyaltyBalanceRepository;
import com.group1.engagement_service.repository.LoyaltyConfigRepository;
import com.group1.engagement_service.repository.TierRepository;
import com.group1.engagement_service.util.LoyaltyValidationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoyaltyConfigService {

    private static final BigDecimal DEFAULT_POINTS_PER_CURRENCY = new BigDecimal("0.0001");
    private static final BigDecimal DEFAULT_MIN_ORDER_AMOUNT = BigDecimal.ZERO;
    private static final int DEFAULT_EXPIRATION_MONTHS = 12;
    private static final int DEFAULT_EVALUATION_PERIOD_MONTHS = 12;

    private final LoyaltyConfigRepository loyaltyConfigRepository;
    private final TierRepository tierRepository;
    private final LoyaltyBalanceRepository loyaltyBalanceRepository;

    @Transactional
    public LoyaltyConfigResponse getConfig() {
        LoyaltyConfig config = getOrCreateConfig();
        List<Tier> tiers = getOrCreateDefaultTiers();
        return buildConfigResponse(config, tiers);
    }

    @Transactional
    public LoyaltyConfigResponse updatePointConfig(PointConfigRequest request) {
        LoyaltyConfig config = getOrCreateConfig();
        List<Tier> tiers = getOrCreateDefaultTiers();

        config.setPointsPerCurrency(request.getPointsPerCurrency());
        config.setMinOrderAmount(request.getMinOrderAmount());
        config.setExcludedCategories(toCsv(request.getExcludedCategories()));

        loyaltyConfigRepository.save(config);
        return buildConfigResponse(config, tiers);
    }

    @Transactional
    public LoyaltyConfigResponse updateExpirationConfig(ExpirationConfigRequest request) {
        LoyaltyConfig config = getOrCreateConfig();
        List<Tier> tiers = getOrCreateDefaultTiers();

        config.setExpirationMonths(request.getExpirationMonths());
        config.setEvaluationPeriodMonths(request.getEvaluationPeriodMonths());

        loyaltyConfigRepository.save(config);
        return buildConfigResponse(config, tiers);
    }

    @Transactional
    public LoyaltyConfigResponse updateTierConfig(TierConfigUpdateRequest request) {
        LoyaltyConfig config = getOrCreateConfig();
        List<Tier> existingTiers = getOrCreateDefaultTiers();
        List<TierDefinitionRequest> requestedTiers = request == null ? null : request.getTiers();

        if (requestedTiers == null || requestedTiers.isEmpty()) {
            throw new BadRequestException("Tier configuration is required");
        }

        List<TierDefinitionRequest> normalizedRequests = normalizeAndValidateTierDefinitions(requestedTiers);
        Map<Long, Tier> existingById = existingTiers.stream()
                .filter(tier -> tier.getId() != null)
                .collect(Collectors.toMap(Tier::getId, Function.identity()));
        Map<String, Tier> existingByName = existingTiers.stream()
                .collect(Collectors.toMap(tier -> tier.getName().toUpperCase(), Function.identity()));

        Set<Long> matchedExistingIds = new LinkedHashSet<>();
        List<Tier> tiersToSave = new ArrayList<>();

        for (TierDefinitionRequest definition : normalizedRequests) {
            Tier tier = resolveExistingTier(definition, existingById, existingByName, matchedExistingIds);
            tier.setName(LoyaltyValidationUtils.normalizeTierName(definition.getName()));
            tier.setMinPoints(definition.getMinPoints());
            tier.setMaxPoints(definition.getMaxPoints());
            replaceTierBenefits(tier, definition.getBenefits());
            tiersToSave.add(tier);
        }

        List<Tier> staleTiers = existingTiers.stream()
                .filter(tier -> !matchedExistingIds.contains(tier.getId()))
                .toList();

        for (Tier staleTier : staleTiers) {
            if (loyaltyBalanceRepository.existsByCurrentTier_Id(staleTier.getId())) {
                throw new BadRequestException("Cannot remove tier " + staleTier.getName() + " because it is assigned to customers");
            }
        }

        if (!staleTiers.isEmpty()) {
            tierRepository.deleteAll(staleTiers);
        }

        List<Tier> savedTiers = tierRepository.saveAll(tiersToSave).stream()
                .sorted(Comparator.comparing(Tier::getMinPoints))
                .toList();

        if (request.getInheritFromLowerTiers() != null) {
            config.setInheritFromLowerTiers(request.getInheritFromLowerTiers());
        }
        loyaltyConfigRepository.save(config);

        return buildConfigResponse(config, savedTiers);
    }

    private LoyaltyConfig getOrCreateConfig() {
        return loyaltyConfigRepository.findById(LoyaltyConfig.SINGLETON_ID)
                .orElseGet(() -> loyaltyConfigRepository.save(LoyaltyConfig.builder()
                        .id(LoyaltyConfig.SINGLETON_ID)
                        .pointsPerCurrency(DEFAULT_POINTS_PER_CURRENCY)
                        .minOrderAmount(DEFAULT_MIN_ORDER_AMOUNT)
                        .excludedCategories("")
                        .expirationMonths(DEFAULT_EXPIRATION_MONTHS)
                        .evaluationPeriodMonths(DEFAULT_EVALUATION_PERIOD_MONTHS)
                        .inheritFromLowerTiers(Boolean.FALSE)
                        .build()));
    }

    private List<Tier> getOrCreateDefaultTiers() {
        List<Tier> tiers = tierRepository.findAllByOrderByMinPointsAsc();
        if (!tiers.isEmpty()) {
            return tiers;
        }

        List<Tier> defaults = List.of(
                Tier.builder().name("BRONZE").minPoints(0).maxPoints(999).benefits(new ArrayList<>()).build(),
                Tier.builder().name("SILVER").minPoints(1000).maxPoints(4999).benefits(new ArrayList<>()).build(),
                Tier.builder().name("GOLD").minPoints(5000).maxPoints(9999).benefits(new ArrayList<>()).build(),
                Tier.builder().name("PLATINUM").minPoints(10000).maxPoints(null).benefits(new ArrayList<>()).build()
        );

        return tierRepository.saveAll(defaults).stream()
                .sorted(Comparator.comparing(Tier::getMinPoints))
                .toList();
    }

    private List<TierDefinitionRequest> normalizeAndValidateTierDefinitions(List<TierDefinitionRequest> requests) {
        List<TierDefinitionRequest> normalized = new ArrayList<>();
        Set<Long> seenIds = new LinkedHashSet<>();
        Set<String> seenNames = new LinkedHashSet<>();

        for (TierDefinitionRequest request : requests) {
            if (request == null) {
                throw new BadRequestException("Tier definition is required");
            }
            if (request.getId() != null && !seenIds.add(request.getId())) {
                throw new BadRequestException("Tier ids must be unique");
            }

            String normalizedName = LoyaltyValidationUtils.normalizeTierName(request.getName());
            if (!seenNames.add(normalizedName)) {
                throw new ConflictException("Tier name already exists");
            }

            Integer minPoints = request.getMinPoints();
            Integer maxPoints = request.getMaxPoints();

            if (minPoints == null || minPoints < 0) {
                throw new BadRequestException("Tier min_points must be greater than or equal to 0");
            }
            if (maxPoints != null && maxPoints < minPoints) {
                throw new BadRequestException("Tier max_points must be greater than or equal to min_points");
            }

            normalized.add(TierDefinitionRequest.builder()
                    .id(request.getId())
                    .name(normalizedName)
                    .minPoints(minPoints)
                    .maxPoints(maxPoints)
                    .benefits(request.getBenefits())
                    .build());
        }

        normalized.sort(Comparator.comparing(TierDefinitionRequest::getMinPoints));
        validateContinuousRanges(normalized);
        return normalized;
    }

    private void validateContinuousRanges(List<TierDefinitionRequest> tiers) {
        if (tiers.get(0).getMinPoints() != 0) {
            throw new BadRequestException("Tier ranges must start from 0");
        }

        for (int index = 0; index < tiers.size(); index++) {
            TierDefinitionRequest current = tiers.get(index);
            boolean isLast = index == tiers.size() - 1;

            if (!isLast && current.getMaxPoints() == null) {
                throw new BadRequestException("Only the highest tier can have no max_points");
            }

            if (!isLast) {
                TierDefinitionRequest next = tiers.get(index + 1);
                int expectedNextMin = current.getMaxPoints() + 1;
                if (!Objects.equals(next.getMinPoints(), expectedNextMin)) {
                    throw new BadRequestException("Tier ranges must be continuous and non-overlapping");
                }
            }
        }
    }

    private Tier resolveExistingTier(
            TierDefinitionRequest definition,
            Map<Long, Tier> existingById,
            Map<String, Tier> existingByName,
            Set<Long> matchedExistingIds) {

        if (definition.getId() != null) {
            Tier existing = existingById.get(definition.getId());
            if (existing == null) {
                throw new ResourceNotFoundException("Tier not found");
            }

            Tier sameNameTier = existingByName.get(definition.getName());
            if (sameNameTier != null && !sameNameTier.getId().equals(existing.getId())) {
                throw new ConflictException("Tier name already exists");
            }

            matchedExistingIds.add(existing.getId());
            return existing;
        }

        Tier existing = existingByName.get(definition.getName());
        if (existing != null) {
            matchedExistingIds.add(existing.getId());
            return existing;
        }

        return Tier.builder().benefits(new ArrayList<>()).build();
    }

    private void replaceTierBenefits(Tier tier, List<TierBenefitRequest> requestedBenefits) {
        tier.getBenefits().clear();
        if (requestedBenefits == null) {
            return;
        }

        for (TierBenefitRequest request : requestedBenefits) {
            String normalizedType = LoyaltyValidationUtils.normalizeBenefitType(request.getType());
            LoyaltyValidationUtils.validateBenefitValue(normalizedType, request.getValue());

            tier.getBenefits().add(TierBenefit.builder()
                    .tier(tier)
                    .type(normalizedType)
                    .value(request.getValue())
                    .description(LoyaltyValidationUtils.normalizeDescription(request.getDescription()))
                    .build());
        }
    }

    private LoyaltyConfigResponse buildConfigResponse(LoyaltyConfig config, List<Tier> tiers) {
        return LoyaltyConfigResponse.builder()
                .pointsPerCurrency(config.getPointsPerCurrency())
                .minOrderAmount(config.getMinOrderAmount())
                .excludedCategories(toCategoryList(config.getExcludedCategories()))
                .expirationMonths(config.getExpirationMonths())
                .evaluationPeriodMonths(config.getEvaluationPeriodMonths())
                .inheritFromLowerTiers(Boolean.TRUE.equals(config.getInheritFromLowerTiers()))
                .tiers(tiers.stream()
                        .sorted(Comparator.comparing(Tier::getMinPoints))
                        .map(this::mapTier)
                        .toList())
                .build();
    }

    private TierConfigResponse mapTier(Tier tier) {
        return TierConfigResponse.builder()
                .id(tier.getId())
                .name(tier.getName())
                .minPoints(tier.getMinPoints())
                .maxPoints(tier.getMaxPoints())
                .benefits(tier.getBenefits().stream()
                        .map(benefit -> TierBenefitResponse.builder()
                                .id(benefit.getId())
                                .type(benefit.getType())
                                .value(benefit.getValue())
                                .description(benefit.getDescription())
                                .inherited(Boolean.FALSE)
                                .sourceTierId(tier.getId())
                                .sourceTierName(tier.getName())
                                .build())
                        .toList())
                .build();
    }

    private String toCsv(List<String> excludedCategories) {
        if (excludedCategories == null || excludedCategories.isEmpty()) {
            return "";
        }

        return excludedCategories.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .distinct()
                .collect(Collectors.joining(","));
    }

    private List<String> toCategoryList(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }

        return List.of(csv.split(",")).stream()
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }
}
