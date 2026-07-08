package com.group1.production_service.service;

import com.group1.production_service.dto.request.BulkAvailabilityUpdateRequest;
import com.group1.production_service.dto.request.FranchiseProductAvailabilityRequest;
import com.group1.production_service.dto.request.ProductAvailabilityRequest;
import com.group1.production_service.dto.response.ProductAvailabilityResponse;

public interface ProductAvailabilityService {
    ProductAvailabilityResponse updateGlobalAvailability(Long productId, ProductAvailabilityRequest request);

    ProductAvailabilityResponse updateFranchiseAvailability(Long productId, Long franchiseId, FranchiseProductAvailabilityRequest request);

    ProductAvailabilityResponse getAvailability(Long productId);

    void bulkUpdateAvailability(BulkAvailabilityUpdateRequest request);
}
