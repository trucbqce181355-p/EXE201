/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.group1.engagement_service.controller;

import com.group1.engagement_service.entity.Coupon;
import com.group1.engagement_service.entity.CouponStatus;
import com.group1.engagement_service.repository.CouponRepository;
import com.group1.engagement_service.request.BulkCreateCouponRequest;
import com.group1.engagement_service.request.CreateCouponRequest;
import com.group1.engagement_service.request.UpdateCouponRequest;
import com.group1.engagement_service.request.ValidateCouponRequest;
import com.group1.engagement_service.service.CouponService;
import com.group1.engagement_service.service.PromotionService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/coupon"})
public class CouponController {

    private final CouponService couponService;
    private final CouponRepository couponRepository;
    private final PromotionService promotionService;

    public CouponController(CouponService couponService, CouponRepository couponRepository, PromotionService promotionService) {
        this.couponService = couponService;
        this.couponRepository = couponRepository;
        this.promotionService = promotionService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('COUPON_CREATE')")
    public ResponseEntity<?> createCoupon(@RequestBody CreateCouponRequest request) {

        System.out.println(request.getCode());
        return ResponseEntity.status(201).body(couponService.createCoupon(request));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAuthority('COUPON_CREATE')")
    public ResponseEntity<?> bulk(@RequestBody BulkCreateCouponRequest req) {
        couponService.bulkCreate(req);
        return ResponseEntity.status(201).build();
    }

    @GetMapping
    
    public ResponseEntity<?> getCoupons(
            @RequestParam(required = false) Long promotionId,
            @RequestParam(required = false) CouponStatus status,
            @RequestParam(required = false) String code,
            Pageable pageable
    ) {

        System.out.println("Status: " + status);
        Page<Coupon> page = couponRepository.filter(promotionId, status, code, pageable);

        return ResponseEntity.ok(
                page.map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", c.getId());
                    map.put("code", c.getCode());
                    map.put("status", c.getStatus());
                    map.put("times_used", c.getTimesUsed());
                    map.put("remaining_uses", c.getMaxUses() == null ? "UNLIMITED" : (c.getMaxUses() - c.getTimesUsed()));
                    map.put("discountValue", c.getPromotion().getValue());
                    map.put("type", c.getPromotion().getType());

                   
                    map.put("promotion", Map.of(
                            "id", c.getPromotion().getId(),
                            "name", c.getPromotion().getName(),
                            "description", c.getPromotion().getDescription() != null ? c.getPromotion().getDescription() : "",
                            "minOrderAmount", c.getPromotion().getMinOrderAmount() != null ? c.getPromotion().getMinOrderAmount() : 0,
                            "maxDiscountAmount", c.getPromotion().getMaxDiscountAmount() != null ? c.getPromotion().getMaxDiscountAmount() : 0
                    ));

                    return map;
                })
        );
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('COUPON_DEACTIVATE')")
    public ResponseEntity<?> deactivate(@PathVariable Long id) {
        couponService.deactivate(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/update")
    @PreAuthorize("hasAuthority('COUPON_UPDATE')")
    public ResponseEntity<?> updateCoupon(
            @PathVariable Long id,
            @RequestBody UpdateCouponRequest req
    ) {
        return ResponseEntity.ok(couponService.updateCoupon(id, req));
    }

    @GetMapping("/promotion")
    public ResponseEntity<?> getPromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(promotionService.getAllActivePromotions(pageable));
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateCoupon(
            @RequestBody ValidateCouponRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return ResponseEntity.ok(couponService.validateCoupon(request, authorization));
    }
}
