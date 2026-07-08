/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.group1.engagement_service.repository;

import com.group1.engagement_service.entity.Coupon;
import com.group1.engagement_service.entity.CouponStatus;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCode(String code);

    @Query("SELECT c FROM Coupon c JOIN FETCH c.promotion WHERE c.code = :code")
    Optional<Coupon> findByCodeFetchPromotion(@Param("code") String code);

    boolean existsByCode(String code);

    @Query("""
        SELECT c FROM Coupon c
        WHERE (:promotionId IS NULL OR c.promotion.id = :promotionId)
        AND (:status IS NULL OR c.status = :status)
        AND (:code IS NULL OR c.code LIKE %:code%)
    """)
    Page<Coupon> filter(
        Long promotionId,
        CouponStatus status,
        String code,
        Pageable pageable
    );
}