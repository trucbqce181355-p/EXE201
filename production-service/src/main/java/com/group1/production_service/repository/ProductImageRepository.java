package com.group1.production_service.repository;

import com.group1.production_service.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProduct_IdOrderByDisplayOrderAscIdAsc(Long productId);

    Optional<ProductImage> findByIdAndProduct_Id(Long id, Long productId);

    long countByProduct_Id(Long productId);
}
