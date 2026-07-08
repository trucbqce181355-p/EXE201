package com.group1.customer_service.controller;

import com.group1.customer_service.dto.LoyaltyResponseDTO;
import com.group1.customer_service.dto.PointsHistoryDTO;
import com.group1.customer_service.dto.RedeemRequestDTO;
import com.group1.customer_service.service.LoyaltyService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    // ==== GET LOYALTY INFO ====
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') ")
    public ResponseEntity<LoyaltyResponseDTO> getLoyalty(@PathVariable Long id) {
        System.out.println("vao loyalty");
        return ResponseEntity.ok(loyaltyService.getLoyalty(id));
    }

    @GetMapping("/{id}/points-history")
    public ResponseEntity<?> getPointsHistory(
            @PathVariable Long id,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        List<PointsHistoryDTO> list = loyaltyService.getPointsHistory(id, type, from, to, page, size);
        return ResponseEntity.ok(list);
    }
}
