package com.group1.engagement_service.util;

import com.group1.engagement_service.entity.BenefitType;
import com.group1.engagement_service.exception.BadRequestException;

import java.math.BigDecimal;

public final class LoyaltyValidationUtils {

    private LoyaltyValidationUtils() {
    }

    public static String normalizeTierName(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Tier name is required");
        }
        return name.trim().toUpperCase();
    }

    public static String normalizeBenefitType(String type) {
        if (type == null || type.isBlank()) {
            throw new BadRequestException("Benefit type is required");
        }

        try {
            return BenefitType.valueOf(type.trim().toUpperCase()).name();
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid benefit type");
        }
    }

    public static String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new BadRequestException("Description is required");
        }
        return description.trim();
    }

    public static void validateBenefitValue(String normalizedType, BigDecimal value) {
        if (BenefitType.DISCOUNT.name().equals(normalizedType)) {
            if (value == null || value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BadRequestException("Discount value must be between 0 and 100");
            }
            return;
        }

        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Benefit value must be greater than or equal to 0");
        }
    }
}
