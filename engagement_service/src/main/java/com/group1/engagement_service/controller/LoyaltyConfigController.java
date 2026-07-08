package com.group1.engagement_service.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group1.engagement_service.dto.request.ExpirationConfigRequest;
import com.group1.engagement_service.dto.request.PointConfigRequest;
import com.group1.engagement_service.dto.request.TierConfigUpdateRequest;
import com.group1.engagement_service.dto.request.TierDefinitionRequest;
import com.group1.engagement_service.dto.response.LoyaltyConfigResponse;
import com.group1.engagement_service.exception.BadRequestException;
import com.group1.engagement_service.service.LoyaltyConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/loyalty", "/api/loyalty"})
@RequiredArgsConstructor
public class LoyaltyConfigController {

    private final LoyaltyConfigService loyaltyConfigService;
    private final ObjectMapper objectMapper;

    @GetMapping("/config")
    @PreAuthorize("hasAuthority('LOYALTY:CONFIG')")
    public ResponseEntity<LoyaltyConfigResponse> getConfig() {
        return ResponseEntity.ok(loyaltyConfigService.getConfig());
    }

    @PutMapping("/config/points")//point number api
    @PreAuthorize("hasAuthority('LOYALTY:CONFIG')")
    public ResponseEntity<LoyaltyConfigResponse> updatePointConfig(@Valid @RequestBody PointConfigRequest request) {
        return ResponseEntity.ok(loyaltyConfigService.updatePointConfig(request));
    }

    @PutMapping("/config/tiers")
    @PreAuthorize("hasAuthority('LOYALTY:CONFIG')")
    public ResponseEntity<LoyaltyConfigResponse> updateTierConfig(@RequestBody JsonNode payload) {
        return ResponseEntity.ok(loyaltyConfigService.updateTierConfig(parseTierConfig(payload)));
    }

    @PutMapping("/config/expiration")
    @PreAuthorize("hasAuthority('LOYALTY:CONFIG')")
    public ResponseEntity<LoyaltyConfigResponse> updateExpirationConfig(@Valid @RequestBody ExpirationConfigRequest request) {
        return ResponseEntity.ok(loyaltyConfigService.updateExpirationConfig(request));
    }

    private TierConfigUpdateRequest parseTierConfig(JsonNode payload) {
        if (payload == null || payload.isNull()) {
            throw new BadRequestException("Tier configuration is required");
        }

        try {
            if (payload.isArray()) {
                List<TierDefinitionRequest> tiers = objectMapper.convertValue(
                        payload,
                        new TypeReference<List<TierDefinitionRequest>>() {
                        }
                );
                return TierConfigUpdateRequest.builder()
                        .tiers(tiers)
                        .build();
            }

            if (payload.isObject()) {
                return objectMapper.treeToValue(payload, TierConfigUpdateRequest.class);
            }
        } catch (Exception ex) {
            throw new BadRequestException("Invalid tier configuration format");
        }

        throw new BadRequestException("Invalid tier configuration format");
    }
}
