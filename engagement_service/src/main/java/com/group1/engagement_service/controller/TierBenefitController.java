package com.group1.engagement_service.controller;

import com.group1.engagement_service.dto.request.TierBenefitRequest;
import com.group1.engagement_service.dto.response.TierBenefitResponse;
import com.group1.engagement_service.service.TierBenefitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/loyalty/tiers", "/api/loyalty/tiers"})
@RequiredArgsConstructor
public class TierBenefitController {

    private final TierBenefitService tierBenefitService;

    @PostMapping("/{tierId}/benefits")
    @PreAuthorize("hasAuthority('LOYALTY:MANAGE_BENEFITS')")
    public ResponseEntity<TierBenefitResponse> createBenefit(
            @PathVariable Long tierId,
            @Valid @RequestBody TierBenefitRequest request) {
        return ResponseEntity.status(201).body(tierBenefitService.createBenefit(tierId, request));
    }

    @GetMapping("/{tierId}/benefits")
    @PreAuthorize("hasAuthority('LOYALTY:MANAGE_BENEFITS')")
    public ResponseEntity<List<TierBenefitResponse>> getBenefits(@PathVariable Long tierId) {
        return ResponseEntity.ok(tierBenefitService.getBenefits(tierId));
    }

    @PutMapping("/{tierId}/benefits/{benefitId}")
    @PreAuthorize("hasAuthority('LOYALTY:MANAGE_BENEFITS')")
    public ResponseEntity<TierBenefitResponse> updateBenefit(
            @PathVariable Long tierId,
            @PathVariable Long benefitId,
            @Valid @RequestBody TierBenefitRequest request) {
        return ResponseEntity.ok(tierBenefitService.updateBenefit(tierId, benefitId, request));
    }

    @DeleteMapping("/{tierId}/benefits/{benefitId}")
    @PreAuthorize("hasAuthority('LOYALTY:MANAGE_BENEFITS')")
    public ResponseEntity<Void> deleteBenefit(
            @PathVariable Long tierId,
            @PathVariable Long benefitId) {
        tierBenefitService.deleteBenefit(tierId, benefitId);
        return ResponseEntity.ok().build();
    }
}
