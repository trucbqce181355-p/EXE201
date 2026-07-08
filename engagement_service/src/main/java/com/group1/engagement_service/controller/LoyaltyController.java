package com.group1.engagement_service.controller;

import com.group1.engagement_service.dto.RedeemRequestDTO;
import com.group1.engagement_service.dto.TierDTO;
import com.group1.engagement_service.dto.CustomerTierDTO;
import com.group1.engagement_service.dto.loyalty.AdjustPointsRequest;
import com.group1.engagement_service.entity.Reward;
import com.group1.engagement_service.request.RedeemDiscountRequest;
import com.group1.engagement_service.response.TierResponse;
import com.group1.engagement_service.security.AuthenticatedUser;
import com.group1.engagement_service.service.LoyaltyService;
import com.group1.engagement_service.service.RewardService;
import com.group1.engagement_service.service.TierService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/loyalty", ""})
public class LoyaltyController {

    private final RewardService rewardService;
    private final TierService tierService;
    private final LoyaltyService loyaltyService;

    public LoyaltyController(RewardService rewardService, TierService tierService, LoyaltyService loyaltyService) {
        this.rewardService = rewardService;
        this.tierService = tierService;
        this.loyaltyService = loyaltyService;
    }

   

    // AC 34.1 - Get rewards
    @GetMapping("/rewards")
    public ResponseEntity<List<Reward>> getRewards() {
        return ResponseEntity.ok(rewardService.getAvailableRewards());
    }
  

    // AC 35.1: Get All Tiers
    @GetMapping("/tiers")
    public ResponseEntity<List<TierResponse>> getAllTiers() {
        System.out.println("vao 8083/loyalty/tiers");
        List<TierResponse> tiers = loyaltyService.findAllTiers();
        return ResponseEntity.ok(tiers);
    }

// AC 35.4: Tier Comparison (Thực tế có thể dùng chung dữ liệu với getAllTiers)
    @GetMapping("/tiers/compare")
    public ResponseEntity<List<TierResponse>> compareTiers() {
        return ResponseEntity.ok(loyaltyService.findAllTiers());
    }
    // MANUAL ADJUST POINTS (ADD/DEDUCT)
    @PostMapping("/customers/{id}/loyalty/points/adjust")
    @PreAuthorize("hasAuthority('LOYALTY:ADJUST_POINTS')")
    public ResponseEntity<?> adjustPoints(
            @PathVariable("id") Long customerId,
            @Valid @RequestBody AdjustPointsRequest request,
            Authentication authentication,
            @RequestHeader(name = "Authorization", required = false) String authorization
    ) {
        AuthenticatedUser actor = null;
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser) {
            actor = (AuthenticatedUser) authentication.getPrincipal();
        }
        loyaltyService.adjustPoints(customerId, request, actor, authorization);
        return ResponseEntity.ok().build();
    }

    // Removed obsolete Tier APIs (tiers list, compare, customer tier, tier-history)

    // VIEW: CUSTOMER POINTS HISTORY (engagement local) - requires authentication
    @GetMapping("/customers/{id}/loyalty/points-history")
    @PreAuthorize("hasAuthority('LOYALTY:ADJUST_POINTS')")
    public ResponseEntity<?> getPointsHistory(@PathVariable("id") Long customerId) {
        return ResponseEntity.ok(loyaltyService.getLocalPointsHistory(customerId));
    }

    // VIEW: CUSTOMER LOYALTY SUMMARY (points, total, tier) from engagement DB
    @GetMapping("/customers/{id}/loyalty/summary")
    @PreAuthorize("hasAuthority('LOYALTY:ADJUST_POINTS')")
    public ResponseEntity<?> getSummary(@PathVariable("id") Long customerId) {
        return ResponseEntity.ok(loyaltyService.getLoyaltySummary(customerId));
    }

    // GET BALANCE (AC 33.1 + 33.2 + 33.3 + 33.4 minimal)
    @GetMapping("/customers/{id}/loyalty/balance")
    @PreAuthorize("hasAuthority('LOYALTY:ADJUST_POINTS')")
    public ResponseEntity<?> getBalance(
            @PathVariable("id") Long customerId,
            @RequestParam(name = "recent", required = false) Integer recent,
            @RequestParam(name = "point_value", required = false) Integer pointValue,
            @RequestParam(name = "currency", required = false) String currency
    ) {
        return ResponseEntity.ok(loyaltyService.getBalance(customerId, recent, pointValue, currency));
    }

    // Secure endpoint for account page: only owner (or admin) can read current points of customerId
    @GetMapping("/customers/{id}/loyalty/current-points")
    @PreAuthorize("hasAuthority('LOYALTY:ADJUST_POINTS')")
    public ResponseEntity<?> getCurrentPoints(
            @PathVariable("id") Long customerId,
            Authentication authentication,
            @RequestHeader(name = "Authorization", required = false) String authorization
    ) {
        AuthenticatedUser actor = null;
        boolean isAdmin = false;
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser) {
            actor = (AuthenticatedUser) authentication.getPrincipal();
            isAdmin = authentication.getAuthorities().stream().anyMatch(a ->
                    "ROLE_ADMIN".equals(a.getAuthority()) || "ADMIN".equals(a.getAuthority()));
        }
        return ResponseEntity.ok(loyaltyService.getCurrentPointsSecure(customerId, actor, isAdmin, authorization));
    }

    // Simpler endpoint for account page: current user only
    @GetMapping("/me/loyalty/current-points")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyCurrentPoints(
            Authentication authentication,
            @RequestHeader(name = "Authorization", required = false) String authorization
    ) {
        AuthenticatedUser actor = null;
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser) {
            actor = (AuthenticatedUser) authentication.getPrincipal();
        }
        return ResponseEntity.ok(loyaltyService.getMyCurrentPoints(actor, authorization));
    }
}
