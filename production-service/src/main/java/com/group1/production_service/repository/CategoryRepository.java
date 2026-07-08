package com.group1.production_service.repository;

import com.group1.production_service.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByDeletedFalseOrderByDisplayOrderAscIdAsc();

    Optional<Category> findByIdAndDeletedFalse(Long id);

    boolean existsBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCaseAndIdNot(String slug, Long id);

    long countByParent_IdAndDeletedFalse(Long parentId);
}
