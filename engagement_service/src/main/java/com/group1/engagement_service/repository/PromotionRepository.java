/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.group1.engagement_service.repository;

import com.group1.engagement_service.entity.Promotion;
import com.group1.engagement_service.entity.PromotionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.group1.engagement_service.entity.PromotionStatus;
import com.group1.engagement_service.entity.PromotionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long>, JpaSpecificationExecutor<Promotion> {

    Optional<Promotion> findByIdAndIsDeletedFalse(Long id);

    boolean existsByNameAndIsDeletedFalse(String name);

    Page<Promotion> findByIsDeletedFalseAndStatus(String status, Pageable pageable);


    Page<Promotion> findByIsDeletedFalse(Pageable pageable);

    @Modifying
    @Query("UPDATE Promotion p SET p.status = :newStatus WHERE p.status = :oldStatus AND p.startDate <= :now AND p.isDeleted = false")
    int updateStatusForStarted(LocalDateTime now, PromotionStatus oldStatus, PromotionStatus newStatus);

    @Modifying
    @Query("UPDATE Promotion p SET p.status = :newStatus WHERE p.status = :oldStatus AND p.endDate <= :now AND p.isDeleted = false")
    int updateStatusForExpired(LocalDateTime now, PromotionStatus oldStatus, PromotionStatus newStatus);


    List<Promotion> findByIsDeletedFalseOrderByIdDesc();

    @Query("""
            SELECT p FROM Promotion p
            WHERE p.isDeleted = false
              AND p.status = :active
              AND p.startDate <= :now AND p.endDate >= :now
              AND (:type IS NULL OR p.type = :type)
            """)
    List<Promotion> findAvailableActiveInWindow(
            @Param("active") PromotionStatus active,
            @Param("now") LocalDateTime now,
            @Param("type") PromotionType type);

    @Query("""
            SELECT p FROM Promotion p
            WHERE p.isDeleted = false
              AND p.status = :active
              AND p.startDate <= :now AND p.endDate >= :now
              AND p.isFeatured = true
              AND (:type IS NULL OR p.type = :type)
            """)
    List<Promotion> findFeaturedActiveInWindow(
            @Param("active") PromotionStatus active,
            @Param("now") LocalDateTime now,
            @Param("type") PromotionType type);
}
