package com.group1.production_service.repository;

import com.group1.production_service.entity.ProductAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductAvailabilityRepository extends JpaRepository<ProductAvailability, Long> {
    List<ProductAvailability> findByProduct_IdOrderByFranchiseIdAsc(Long productId);

    Optional<ProductAvailability> findByProduct_IdAndFranchiseId(Long productId, Long franchiseId);
}
