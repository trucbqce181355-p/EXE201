package com.group1.production_service.repository;

import com.group1.production_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findByIdAndDeletedFalse(Long id);

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, Long id);

    long countByCategory_IdAndDeletedFalse(Long categoryId);

    boolean existsByCategory_IdAndDeletedFalse(Long categoryId);
}
