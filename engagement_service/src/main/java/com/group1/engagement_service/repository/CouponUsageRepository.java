package com.group1.engagement_service.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.group1.engagement_service.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

     boolean existsByCouponIdAndCustomerId(Long couponId, Long customerId);

     long countByCoupon_Promotion_Id(Long promotionId);

     @Query("""
               SELECT COALESCE(SUM(cu.discountAmount), 0)
               FROM CouponUsage cu
               WHERE cu.coupon.promotion.id = :promotionId
               """)
     BigDecimal totalDiscountByPromotion(@Param("promotionId") Long promotionId);

     @Query("""
               SELECT COALESCE(SUM(cu.discountAmount), 0)
               FROM CouponUsage cu
               WHERE cu.coupon.promotion.id = :promotionId
               """)
     BigDecimal totalGrossRevenueByPromotion(@Param("promotionId") Long promotionId);
     @Query("""
               SELECT CAST(cu.usedAt AS date) as date, COUNT(cu.id) as usageCount
               FROM CouponUsage cu
               WHERE cu.coupon.promotion.id = :promotionId
               GROUP BY CAST(cu.usedAt AS date)
               ORDER BY date ASC
               """)
     List<Object[]> getUsageHistoryData(@Param("promotionId") Long promotionId);
     @Query("""
               SELECT COUNT(DISTINCT cu.customerId)
               FROM CouponUsage cu
               WHERE cu.coupon.promotion.id = :promotionId
               """)
     long countUniqueCustomers(@Param("promotionId") Long promotionId);

     @Query("""
               SELECT cu.customerId, COUNT(cu.id)
               FROM CouponUsage cu
               WHERE cu.coupon.promotion.id = :promotionId
               GROUP BY cu.customerId
               """)
     List<Object[]> getCustomerUsageStats(@Param("promotionId") Long promotionId);
}