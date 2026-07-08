package com.group1.engagement_service.controller;

import com.group1.engagement_service.entity.PromotionStatus;
import com.group1.engagement_service.dto.PreviewReachResponse;
import com.group1.engagement_service.dto.PromotionListItemDto;
import com.group1.engagement_service.dto.PromotionPublicResponse;
import com.group1.engagement_service.dto.SetTargetSegmentsRequest;
import com.group1.engagement_service.dto.TargetSegmentsResponse;
import com.group1.engagement_service.entity.PromotionType;
import com.group1.engagement_service.request.PromotionRequest;
import com.group1.engagement_service.response.PromotionResponse;
import com.group1.engagement_service.service.PromotionService;
import com.group1.engagement_service.service.PromotionCatalogService;
import com.group1.engagement_service.service.PromotionManagementService;
import com.group1.engagement_service.service.PromotionTargetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;
    private final PromotionTargetingService promotionTargetingService;
    private final PromotionManagementService promotionManagementService;
    private final PromotionCatalogService promotionCatalogService;

    // Create Promotion
    @PostMapping
    @PreAuthorize("hasAuthority('PROMOTION:CREATE')")
    public ResponseEntity<PromotionResponse> createPromotion(@Valid @RequestBody PromotionRequest request) {
        PromotionResponse response = promotionService.createPromotion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get List Promotions (Pageable)
    @GetMapping
    @PreAuthorize("hasAuthority('PROMOTION:READ')")
    public ResponseEntity<Page<PromotionResponse>> getPromotions(
            @RequestParam(required = false) PromotionStatus status,
            @RequestParam(required = false) PromotionType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PromotionResponse> response = promotionService.getPromotions(status, type, pageable);
        return ResponseEntity.ok(response);
    }

    // Get Promotion by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROMOTION:READ')")
    public ResponseEntity<PromotionResponse> getPromotionById(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.getPromotionById(id));
    }

    // Update Promotion
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROMOTION:UPDATE')")
    public ResponseEntity<PromotionResponse> updatePromotion(
            @PathVariable Long id,
            @Valid @RequestBody PromotionRequest request) {
        return ResponseEntity.ok(promotionService.updatePromotion(id, request));
    }

    // Delete Promotion 
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROMOTION:DELETE')")
    public ResponseEntity<String> deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.ok("Promotion deleted successfully");
    }

    // Activate Promotion
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('PROMOTION:UPDATE')")
    public ResponseEntity<PromotionResponse> activatePromotion(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.changeStatus(id, PromotionStatus.ACTIVE));
    }

    // Deactivate Promotion
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('PROMOTION:UPDATE')")
    public ResponseEntity<PromotionResponse> changeStatus(
            @PathVariable Long id,
            @RequestParam PromotionStatus status) {
        return ResponseEntity.ok(promotionService.changeStatus(id, status));
    }

    /**
     * Danh sách khuyến mãi đang hiệu lực (public). Có thể lọc category, type.
     * Có Bearer (đăng nhập), backend tự resolve customer từ JWT + customer-service, rồi lọc theo segment. Không cần query {@code customer_id}.
     */
    @GetMapping("/available")
    public ResponseEntity<List<PromotionPublicResponse>> getAvailable(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "type", required = false) PromotionType type,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return ResponseEntity.ok(promotionCatalogService.getAvailable(category, type, authorization));
    }

    /**
     * Khuyến mãi nổi bật (banner / trang chủ). Cùng quy tắc lọc segment như {@code /available} khi có Bearer.
     */
    @GetMapping("/featured")
    public ResponseEntity<List<PromotionPublicResponse>> getFeatured(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "type", required = false) PromotionType type,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return ResponseEntity.ok(promotionCatalogService.getFeatured(category, type, authorization));
    }


    @PostMapping("/{id}/target-segments")
    public ResponseEntity<TargetSegmentsResponse> assignTargetSegments(
            @PathVariable("id") Long promotionId,
            @Valid @RequestBody SetTargetSegmentsRequest body,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        promotionTargetingService.assignTargetSegments(promotionId, body, authorization);
        return ResponseEntity.ok(promotionTargetingService.getTargetSegments(promotionId, authorization));
    }

    @DeleteMapping("/{id}/target-segments/{segmentId}")
    public ResponseEntity<Void> removeTargetSegment(
            @PathVariable("id") Long promotionId,
            @PathVariable("segmentId") Long segmentId) {
        promotionTargetingService.removeTargetSegment(promotionId, segmentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{id}/target-segments")
    public ResponseEntity<TargetSegmentsResponse> getTargetSegments(
            @PathVariable("id") Long promotionId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return ResponseEntity.ok(promotionTargetingService.getTargetSegments(promotionId, authorization));
    }

    @GetMapping("/{id}/preview-reach")
    public ResponseEntity<PreviewReachResponse> previewReach(
            @PathVariable("id") Long promotionId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return ResponseEntity.ok(promotionTargetingService.previewReach(promotionId, authorization));
    }
}