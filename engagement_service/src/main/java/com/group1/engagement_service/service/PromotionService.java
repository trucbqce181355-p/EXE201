package com.group1.engagement_service.service;

import com.group1.engagement_service.entity.Promotion;
import com.group1.engagement_service.entity.PromotionStatus;
import com.group1.engagement_service.entity.PromotionType;
import com.group1.engagement_service.repository.PromotionRepository;
import com.group1.engagement_service.repository.PromotionSpecification;
import com.group1.engagement_service.request.PromotionRequest;
import com.group1.engagement_service.response.PromotionResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;

    // ==========================================
    // HELPER
    // ==========================================
    private void validatePromotionRules(PromotionRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        if (request.getType() == PromotionType.PERCENTAGE_DISCOUNT
                && request.getValue().compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Invalid discount percentage");
        }
    }

    // ==========================================
    // 1. CREATE PROMOTION
    // ==========================================
    @Transactional
    public PromotionResponse createPromotion(PromotionRequest request) {
        validatePromotionRules(request);
        Promotion promotion = Promotion.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .value(request.getValue())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(PromotionStatus.DRAFT)
                .isFeatured(request.getFeatured() != null ? request.getFeatured() : false)
                .minOrderAmount(request.getMinOrderAmount())
                .minQuantity(request.getMinQuantity())
                .applicableProducts(request.getApplicableProducts())
                .applicableCategories(request.getApplicableCategories())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .maxUsesTotal(request.getMaxUsesTotal())
                .maxUsesPerCustomer(request.getMaxUsesPerCustomer())
                .isDeleted(false)
                .build();

        Promotion savedPromotion = promotionRepository.save(promotion);
        return mapToResponse(savedPromotion);
    }

    // ==========================================
    // 2. GET LIST PROMOTIONS
    // ==========================================
    public Page<PromotionResponse> getPromotions(PromotionStatus status, PromotionType type, Pageable pageable) {
        Page<Promotion> promotions = promotionRepository.findAll(
                PromotionSpecification.filterPromotions(status, type),
                pageable
        );
        return promotions.map(this::mapToResponse);
    }

    // ==========================================
    // 3. UPDATE PROMOTION
    // ==========================================
    @Transactional
    public PromotionResponse updatePromotion(Long id, PromotionRequest request) {
        Promotion promotion = promotionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        validatePromotionRules(request);
        if (promotion.getStatus() == PromotionStatus.ACTIVE || promotion.getStatus() == PromotionStatus.SCHEDULED) {
            if (request.getEndDate().isBefore(promotion.getEndDate())) {
                throw new IllegalArgumentException("Cannot shorten end date of an ACTIVE promotion");
            }
            promotion.setEndDate(request.getEndDate());
        }
        else if (promotion.getStatus() == PromotionStatus.DRAFT) {
            promotion.setName(request.getName());
            promotion.setDescription(request.getDescription());
            promotion.setType(request.getType());
            promotion.setValue(request.getValue());
            promotion.setStartDate(request.getStartDate());
            promotion.setEndDate(request.getEndDate());
            promotion.setIsFeatured(request.getFeatured() != null ? request.getFeatured() : false);

            promotion.setMinOrderAmount(request.getMinOrderAmount());
            promotion.setMinQuantity(request.getMinQuantity());
            promotion.setApplicableProducts(request.getApplicableProducts());
            promotion.setApplicableCategories(request.getApplicableCategories());
            promotion.setMaxDiscountAmount(request.getMaxDiscountAmount());
            promotion.setMaxUsesTotal(request.getMaxUsesTotal());
            promotion.setMaxUsesPerCustomer(request.getMaxUsesPerCustomer());
        }
        else {
            throw new IllegalArgumentException("Cannot update promotion in current status");
        }

        Promotion updatedPromotion = promotionRepository.save(promotion);
        return mapToResponse(updatedPromotion);
    }

    // ==========================================
    // 4. DELETE PROMOTION
    // ==========================================
    @Transactional
    public void deletePromotion(Long id) {
        Promotion promotion = promotionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        if (promotion.getStatus() == PromotionStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot delete active promotion");
        }

        promotion.setIsDeleted(true);
        promotionRepository.save(promotion);
    }

    // ==========================================
    // 5. ACTIVATE / DEACTIVATE
    // ==========================================
    @Transactional
    public PromotionResponse changeStatus(Long id, PromotionStatus newStatus) {
        Promotion promotion = promotionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        if (newStatus == PromotionStatus.ACTIVE) {
            LocalDateTime now = LocalDateTime.now();

            if (promotion.getStartDate().isAfter(now)) {
                promotion.setStatus(PromotionStatus.SCHEDULED);
            }
            else {
                promotion.setStatus(PromotionStatus.ACTIVE);
            }
        } else {
            promotion.setStatus(newStatus);
        }

        return mapToResponse(promotionRepository.save(promotion));
    }

    // ==========================================
    // 6. GET PROMOTION BY ID
    // ==========================================
    public PromotionResponse getPromotionById(Long id) {
        Promotion promotion = promotionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));
        return mapToResponse(promotion);
    }

    private PromotionStatus calculateStatus(LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime now = LocalDateTime.now();

        if (endDate.isBefore(now)) {
            return PromotionStatus.EXPIRED;
        }
        if (startDate.isAfter(now)) {
            return PromotionStatus.SCHEDULED;
        }
        return PromotionStatus.ACTIVE;
    }

    public Page<Map<String, Object>> getAllActivePromotions(Pageable pageable) {
        // Lấy danh sách từ DB
        Page<Promotion> promotions = promotionRepository.findByIsDeletedFalse(pageable);

        // Map sang DTO hoặc Map để trả về những trường FE cần thôi
        return promotions.map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName()); // Đây là trường FE dùng để hiển thị nè đại ca
            map.put("type", p.getType());
            map.put("value", p.getValue());
            map.put("status", p.getStatus());
            return map;
        });
    }

    // ==========================================
    // HELPER
    // ==========================================
    private PromotionResponse mapToResponse(Promotion promotion) {
        return PromotionResponse.builder()
                .id(promotion.getId())
                .name(promotion.getName())
                .description(promotion.getDescription())
                .type(promotion.getType())
                .status(promotion.getStatus())
                .value(promotion.getValue())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .isFeatured(promotion.getIsFeatured())
                .createdAt(promotion.getCreatedAt())
                .updatedAt(promotion.getUpdatedAt())
                .minOrderAmount(promotion.getMinOrderAmount())
                .minQuantity(promotion.getMinQuantity())
                .applicableProducts(promotion.getApplicableProducts())
                .applicableCategories(promotion.getApplicableCategories())
                .maxDiscountAmount(promotion.getMaxDiscountAmount())
                .maxUsesTotal(promotion.getMaxUsesTotal())
                .maxUsesPerCustomer(promotion.getMaxUsesPerCustomer())
                .totalUses(0)
                .build();
    }
}