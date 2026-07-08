package com.group1.production_service.controller;

import com.group1.production_service.dto.request.BulkAvailabilityUpdateRequest;
import com.group1.production_service.dto.request.FranchiseProductAvailabilityRequest;
import com.group1.production_service.dto.request.ProductAvailabilityRequest;
import com.group1.production_service.dto.response.ApiResponse;
import com.group1.production_service.dto.response.ProductAvailabilityResponse;
import com.group1.production_service.service.ProductAvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductAvailabilityController {

    private final ProductAvailabilityService productAvailabilityService;

    @PutMapping("/{id}/availability")
    @PreAuthorize("hasAuthority('PRODUCT:UPDATE')")
    public ResponseEntity<ApiResponse<ProductAvailabilityResponse>> updateGlobalAvailability(
            @PathVariable Long id,
            @Valid @RequestBody ProductAvailabilityRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Product availability updated", productAvailabilityService.updateGlobalAvailability(id, request)));
    }

    @PutMapping("/{id}/availability/franchise/{franchiseId}")
    @PreAuthorize("hasAuthority('PRODUCT:UPDATE')")
    public ResponseEntity<ApiResponse<ProductAvailabilityResponse>> updateFranchiseAvailability(
            @PathVariable Long id,
            @PathVariable Long franchiseId,
            @Valid @RequestBody FranchiseProductAvailabilityRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Franchise availability updated", productAvailabilityService.updateFranchiseAvailability(id, franchiseId, request)));
    }

    @GetMapping("/{id}/availability")
    @PreAuthorize("hasAuthority('PRODUCT:READ')")
    public ResponseEntity<ApiResponse<ProductAvailabilityResponse>> getAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Product availability fetched", productAvailabilityService.getAvailability(id)));
    }

    @PutMapping("/bulk/availability")
    @PreAuthorize("hasAuthority('PRODUCT:UPDATE')")
    public ResponseEntity<ApiResponse<Void>> bulkUpdateAvailability(@Valid @RequestBody BulkAvailabilityUpdateRequest request) {
        productAvailabilityService.bulkUpdateAvailability(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Product availability updated", null));
    }
}
