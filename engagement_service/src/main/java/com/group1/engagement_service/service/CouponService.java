/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.group1.engagement_service.service;

import com.group1.engagement_service.dto.ValidateCouponResponse;
import com.group1.engagement_service.entity.Coupon;
import com.group1.engagement_service.entity.CouponStatus;
import com.group1.engagement_service.entity.Promotion;
import com.group1.engagement_service.entity.PromotionStatus;
import com.group1.engagement_service.repository.CouponRepository;
import com.group1.engagement_service.repository.PromotionRepository;
import com.group1.engagement_service.request.BulkCreateCouponRequest;
import com.group1.engagement_service.request.CreateCouponRequest;
import com.group1.engagement_service.request.UpdateCouponRequest;
import com.group1.engagement_service.request.ValidateCouponRequest;
import com.group1.engagement_service.request.ValidateCouponRequest;
import static jakarta.persistence.GenerationType.IDENTITY;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import org.springframework.web.server.ResponseStatusException;

@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final PromotionRepository promotionRepository;
    private final PromotionTargetingService promotionTargetingService;

    public CouponService(
            CouponRepository couponRepository,
            PromotionRepository promotionRepository,
            PromotionTargetingService promotionTargetingService) {
        this.couponRepository = couponRepository;
        this.promotionRepository = promotionRepository;
        this.promotionTargetingService = promotionTargetingService;
    }

    @Transactional
    public Coupon createCoupon(CreateCouponRequest request) {
        String code = request.getCode();

        if (code == null || code.isBlank()) {
            code = generateCode("AUTO");
        }

        validateCodeFormat(code);

        if (couponRepository.existsByCode(code)) {
            throw new RuntimeException("Coupon code already exists");
        }

        Promotion promotion = promotionRepository.findById(request.getPromotionId())
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        Coupon coupon = Coupon.builder()
                .code(code)
                .promotion(promotion)
                .maxUses(request.getMaxUses())
                .timesUsed(0)
                .status(CouponStatus.ACTIVE)
                .build();

        return couponRepository.save(coupon);
    }

    @Transactional
    public void bulkCreate(BulkCreateCouponRequest req) {

        Promotion promotion = promotionRepository.findById(req.getPromotionId())
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        List<Coupon> list = new ArrayList<>();

        for (int i = 0; i < req.getQuantity(); i++) {
            String code;
            do {
                code = generateCode(req.getPrefix());
            } while (couponRepository.existsByCode(code));

            list.add(Coupon.builder()
                    .code(code)
                    .promotion(promotion)
                    .maxUses(req.getMaxUsesPerCode())
                    .timesUsed(0)
                    .status(CouponStatus.ACTIVE)
                    .build());
        }

        couponRepository.saveAll(list);
    }

    @Transactional
    public Coupon updateCoupon(Long id, UpdateCouponRequest req) {

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (req.getMaxUses() != null) {
            if (req.getMaxUses() < coupon.getTimesUsed()) {
                throw new RuntimeException("maxUses cannot be less than timesUsed");
            }
            coupon.setMaxUses(req.getMaxUses());
        }

        if (req.getStatus() != null) {
            coupon.setStatus(req.getStatus());
        }

        validateCodeFormat(req.getCode());
        if (!coupon.getCode().equals(req.getCode())) {
            if (couponRepository.existsByCode(req.getCode())) {
                throw new RuntimeException("Coupon code already exists");
            }
        }
        coupon.setCode(req.getCode());

        return couponRepository.save(coupon);
    }

    public void deactivate(Long id) {
        Coupon c = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        c.setStatus(CouponStatus.INACTIVE);
        couponRepository.save(c);
    }

    private String generateCode(String prefix) {
        return prefix + "-"
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase()
                + "-"
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private void validateCodeFormat(String code) {
        if (code == null || !code.matches("^[A-Z0-9-]+$")) {
            throw new IllegalArgumentException("Code format invalid (only A-Z, 0-9, - allowed)");
        }
    }

    public Coupon validateCoupon(ValidateCouponRequest req) {

        Coupon coupon = couponRepository.findByCode(req.getCode())
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (!coupon.getStatus().name().equals("ACTIVE")) {
            throw new RuntimeException("Coupon inactive");
        }

        if (coupon.getMaxUses() != null
                && coupon.getTimesUsed() >= coupon.getMaxUses()) {
            throw new RuntimeException("Coupon exhausted");
        }

        Promotion promo = coupon.getPromotion();

        if (promo.getEndDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Expired");
        }

//        if (req.getOrderAmount().compareTo(promo.getMinOrderAmount()) < 0) {
//            throw new RuntimeException("Min order not met");
//        }

        return coupon;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ValidateCouponResponse validateCoupon(ValidateCouponRequest request, String authorizationHeader) {
        if (request == null || request.getCode() == null || request.getCode().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coupon code is required");
        }
        String code = request.getCode().trim();
        Coupon coupon = couponRepository.findByCodeFetchPromotion(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found"));
        Promotion promotion = coupon.getPromotion();

        if (!CouponStatus.ACTIVE.name().equalsIgnoreCase(coupon.getStatus().name())) {
            return new ValidateCouponResponse(false, "Coupon is not active.", promotion.getId(), coupon.getId());
        }
        Integer maxUses = coupon.getMaxUses();
        Integer timesUsed = coupon.getTimesUsed();
        if (maxUses != null && timesUsed != null && timesUsed >= maxUses) {
            return new ValidateCouponResponse(false, "Coupon usage limit reached.", promotion.getId(), coupon.getId());
        }
        if (Boolean.TRUE.equals(promotion.getIsDeleted())) {
            return new ValidateCouponResponse(false, "Promotion is no longer available.", promotion.getId(), coupon.getId());
        }
        if (promotion.getStatus() != PromotionStatus.ACTIVE) {
            return new ValidateCouponResponse(false, "Promotion is not active.", promotion.getId(), coupon.getId());
        }
        LocalDateTime now = LocalDateTime.now();
        if (promotion.getStartDate() != null && now.isBefore(promotion.getStartDate())) {
            return new ValidateCouponResponse(false, "Promotion has not started yet.", promotion.getId(), coupon.getId());
        }
        if (promotion.getEndDate() != null && now.isAfter(promotion.getEndDate())) {
            return new ValidateCouponResponse(false, "Promotion has ended.", promotion.getId(), coupon.getId());
        }
        if (!promotionTargetingService.isCustomerEligible(promotion, request.getCustomerId(), authorizationHeader)) {
            return new ValidateCouponResponse(
                    false,
                    "Customer is not eligible for this promotion (segment targeting).",
                    promotion.getId(),
                    coupon.getId());
        }
        return new ValidateCouponResponse(true, "Coupon is valid.", promotion.getId(), coupon.getId());
    }

}
