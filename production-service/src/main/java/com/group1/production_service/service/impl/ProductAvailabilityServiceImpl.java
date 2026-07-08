package com.group1.production_service.service.impl;

import com.group1.production_service.dto.request.BulkAvailabilityUpdateRequest;
import com.group1.production_service.dto.request.FranchiseProductAvailabilityRequest;
import com.group1.production_service.dto.request.ProductAvailabilityRequest;
import com.group1.production_service.dto.response.ProductAvailabilityItemResponse;
import com.group1.production_service.dto.response.ProductAvailabilityResponse;
import com.group1.production_service.entity.Product;
import com.group1.production_service.entity.ProductAvailability;
import com.group1.production_service.exception.BadRequestException;
import com.group1.production_service.exception.ResourceNotFoundException;
import com.group1.production_service.repository.ProductAvailabilityRepository;
import com.group1.production_service.repository.ProductRepository;
import com.group1.production_service.service.ProductAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductAvailabilityServiceImpl implements ProductAvailabilityService {

    private final ProductRepository productRepository;
    private final ProductAvailabilityRepository productAvailabilityRepository;

    @Override
    @Transactional
    public ProductAvailabilityResponse updateGlobalAvailability(Long productId, ProductAvailabilityRequest request) {
        Product product = getProductEntity(productId);
        if (request.getIsAvailable() == null) {
            throw new BadRequestException("Availability flag is required");
        }
        validateDateRange(request.getAvailableFrom(), request.getAvailableUntil());

        product.setAvailable(request.getIsAvailable());
        product.setAvailableFrom(request.getAvailableFrom());
        product.setAvailableUntil(request.getAvailableUntil());
        if (request.getAutoUnavailableWhenOutOfStock() != null) {
            product.setAutoUnavailableWhenOutOfStock(request.getAutoUnavailableWhenOutOfStock());
        }

        productRepository.save(product);
        return buildResponse(product);
    }

    @Override
    @Transactional
    public ProductAvailabilityResponse updateFranchiseAvailability(Long productId, Long franchiseId, FranchiseProductAvailabilityRequest request) {
        Product product = getProductEntity(productId);
        if (request.getIsAvailable() == null) {
            throw new BadRequestException("Availability flag is required");
        }
        validateDateRange(request.getAvailableFrom(), request.getAvailableUntil());

        ProductAvailability availability = productAvailabilityRepository.findByProduct_IdAndFranchiseId(productId, franchiseId)
                .orElse(ProductAvailability.builder()
                        .product(product)
                        .franchiseId(franchiseId)
                        .build());

        availability.setAvailable(request.getIsAvailable());
        availability.setPriceOverride(request.getPriceOverride());
        availability.setAvailableFrom(request.getAvailableFrom());
        availability.setAvailableUntil(request.getAvailableUntil());

        productAvailabilityRepository.save(availability);
        return buildResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductAvailabilityResponse getAvailability(Long productId) {
        return buildResponse(getProductEntity(productId));
    }

    @Override
    @Transactional
    public void bulkUpdateAvailability(BulkAvailabilityUpdateRequest request) {
        List<Product> products = productRepository.findAllById(request.getProductIds()).stream()
                .filter(product -> !Boolean.TRUE.equals(product.getDeleted()))
                .toList();

        if (products.size() != request.getProductIds().size()) {
            throw new ResourceNotFoundException("Product not found");
        }

        products.forEach(product -> product.setAvailable(request.getIsAvailable()));
        productRepository.saveAll(products);
    }

    private Product getProductEntity(Long productId) {
        return productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private void validateDateRange(LocalDate from, LocalDate until) {
        if (from != null && until != null && until.isBefore(from)) {
            throw new BadRequestException("End date must be after start date");
        }
    }

    private ProductAvailabilityResponse buildResponse(Product product) {
        return ProductAvailabilityResponse.builder()
                .productId(product.getId())
                .globalAvailable(product.getAvailable())
                .availableFrom(product.getAvailableFrom())
                .availableUntil(product.getAvailableUntil())
                .autoUnavailableWhenOutOfStock(product.getAutoUnavailableWhenOutOfStock())
                .franchises(productAvailabilityRepository.findByProduct_IdOrderByFranchiseIdAsc(product.getId()).stream()
                        .map(availability -> ProductAvailabilityItemResponse.builder()
                                .franchiseId(availability.getFranchiseId())
                                .available(availability.getAvailable())
                                .priceOverride(availability.getPriceOverride())
                                .availableFrom(availability.getAvailableFrom())
                                .availableUntil(availability.getAvailableUntil())
                                .build())
                        .toList())
                .build();
    }
}
