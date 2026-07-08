package com.group1.production_service.repository;

import com.group1.production_service.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsBySlug(String slug);
}
