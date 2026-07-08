/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.group1.engagement_service.controller;

import com.group1.engagement_service.dto.LoyaltyResponseDTO;
import com.group1.engagement_service.entity.LoyaltyRedemption;
import com.group1.engagement_service.entity.Reward;
import com.group1.engagement_service.repository.LoyaltyRedemptionRepository;
import com.group1.engagement_service.repository.RewardRepository;
import com.group1.engagement_service.request.RedeemRequest;
import com.group1.engagement_service.response.RedemptionResponse;
import com.group1.engagement_service.response.TierResponse;
import com.group1.engagement_service.service.LoyaltyService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    private final LoyaltyService loyaltyService;
    private final LoyaltyRedemptionRepository loyaltyRedemptionRepository;
    private final RewardRepository rewardRepository;

    public CustomerController(LoyaltyService loyaltyService, LoyaltyRedemptionRepository loyaltyRedemptionRepository, RewardRepository rewardRepository) {
        this.loyaltyService = loyaltyService;
        this.loyaltyRedemptionRepository = loyaltyRedemptionRepository;
        this.rewardRepository = rewardRepository;
    }

    @PostMapping("/{id}/loyalty/redeem")
    public ResponseEntity<?> redeem(
            @PathVariable Long id,
            @RequestBody RedeemRequest request,
            HttpServletRequest httpRequest
    ) {

        String token = httpRequest.getHeader("Authorization");

        loyaltyService.redeem(id, request, token);

        return ResponseEntity.ok("ok");
    }

    @GetMapping("/{id}/loyalty/redemptions")
    public ResponseEntity<List<RedemptionResponse>> getHistory(@PathVariable Long id) {

        return ResponseEntity.ok(
                loyaltyService.getRedemptionHistory(id)
        );
    }

    @GetMapping("/loyalty/available-rewards")
    public ResponseEntity<List<Reward>> getAvailableRewards() {
        return ResponseEntity.ok(rewardRepository.findByAvailabilityTrue());
    }

    @GetMapping("/{id}/loyalty")
    public ResponseEntity<LoyaltyResponseDTO> getLoyalty(@PathVariable Long id) {

        return ResponseEntity.ok(
                loyaltyService.getLoyalty(id)
        );
    }

    @GetMapping("/{id}/loyalty/points-history")
    public ResponseEntity<?> getPointsHistory(
            @PathVariable Long id,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        System.out.println("fillter type: " + type);
        return ResponseEntity.ok(
                loyaltyService.getPointsHistory(id, type, from, to, page, size));
    }

    // GET TIER HISTORY
    @GetMapping("/{id}/loyalty/tier-history")
    public ResponseEntity<?> getTierHistory(@PathVariable Long id) {
        System.out.println("Vào GET TIER HISTORY");
        return ResponseEntity.ok(loyaltyService.getTierHistory(id));
    }

    
    
}
