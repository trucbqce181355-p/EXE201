package com.group1.customer_service.repository;

import com.group1.customer_service.entity.Order;
import com.group1.customer_service.repository.projection.SegmentCustomerProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // --- CÁC PHƯƠNG THỨC TỪ ĐOẠN 1 ---

    @Query("""
        SELECT o FROM Order o
        WHERE o.customer.customerId = :customerId
        AND (:status IS NULL OR o.status IN :status)
        AND (:fromDate IS NULL OR o.createdAt >= :fromDate)
        AND (:toDate IS NULL OR o.createdAt <= :toDate)
    """)
    Page<Order> filterOrders(
            @Param("customerId") Long customerId,
            @Param("status") List<String> status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("""
        SELECT o FROM Order o
        WHERE (:status IS NULL OR o.status IN :status)
        AND (:fromDate IS NULL OR o.createdAt >= :fromDate)
        AND (:toDate IS NULL OR o.createdAt <= :toDate)
    """)
    Page<Order> filterAllOrders(
            @Param("status") List<String> status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    @Query("""
        SELECT o FROM Order o
        LEFT JOIN FETCH o.items
        LEFT JOIN FETCH o.payment
        LEFT JOIN FETCH o.delivery
        LEFT JOIN FETCH o.statusHistories
        WHERE o.orderId = :orderId
        AND o.customer.customerId = :customerId
    """)
    Optional<Order> findOrderDetail(
            @Param("orderId") Long orderId,
            @Param("customerId") Long customerId
    );

    long countByCustomer_CustomerId(Long customerId);

    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE o.customer.customerId = :customerId
    """)
    BigDecimal sumTotalAmountByCustomerId(@Param("customerId") Long customerId);

    @Query("""
        SELECT MAX(o.createdAt)
        FROM Order o
        WHERE o.customer.customerId = :customerId
    """)
    LocalDateTime findLastOrderDateByCustomerId(@Param("customerId") Long customerId);

    List<Order> findTop5ByCustomer_CustomerIdOrderByCreatedAtDesc(Long customerId);


    // --- CÁC PHƯƠNG THỨC TỪ ĐOẠN 2 (PHÂN ĐOẠN KHÁCH HÀNG) ---

    @Query(value = """
            select
                c.customer_id as id,
                c.user_id as userId,
                coalesce(sum(o.total_amount), 0) as totalSpent,
                count(o.order_id) as orderCount,
                date(max(o.created_at)) as lastOrderDate,
                l.current_tier as loyaltyTier,
                coalesce((
                    select group_concat(distinct ao.address_line order by ao.address_line separator ', ')
                    from address_orders ao
                    where ao.customer_id = c.customer_id
                ), '-') as location
            from customers c
            left join loyalty l on l.customer_id = c.customer_id
            left join orders o on o.customer_id = c.customer_id
            group by c.customer_id, c.user_id, l.current_tier
            having (
                (:logic = 'AND')
                and (
                    :totalSpent is null or
                    (:conditionSpent = '=' and coalesce(sum(o.total_amount), 0) = :totalSpent) or
                    (:conditionSpent = '>' and coalesce(sum(o.total_amount), 0) > :totalSpent) or
                    (:conditionSpent = '<' and coalesce(sum(o.total_amount), 0) < :totalSpent) or
                    (:conditionSpent = '>=' and coalesce(sum(o.total_amount), 0) >= :totalSpent) or
                    (:conditionSpent = '<=' and coalesce(sum(o.total_amount), 0) <= :totalSpent)
                )
                and (
                    :orderCount is null or
                    (:conditionCount = '=' and count(o.order_id) = :orderCount) or
                    (:conditionCount = '>' and count(o.order_id) > :orderCount) or
                    (:conditionCount = '<' and count(o.order_id) < :orderCount) or
                    (:conditionCount = '>=' and count(o.order_id) >= :orderCount) or
                    (:conditionCount = '<=' and count(o.order_id) <= :orderCount)
                )
                and (
                    :lastOrderDate is null or
                    (:conditionDate = '=' and date(max(o.created_at)) = :lastOrderDate) or
                    (:conditionDate = '>' and date(max(o.created_at)) > :lastOrderDate) or
                    (:conditionDate = '<' and date(max(o.created_at)) < :lastOrderDate) or
                    (:conditionDate = '>=' and date(max(o.created_at)) >= :lastOrderDate) or
                    (:conditionDate = '<=' and date(max(o.created_at)) <= :lastOrderDate)
                )
                and (
                    :loyaltyTier is null or
                    (:conditionTier = 'IN' and find_in_set(l.current_tier, :loyaltyTier) > 0) or
                    (:conditionTier = 'NOT_IN' and find_in_set(l.current_tier, :loyaltyTier) = 0)
                )
                and (
                    :location is null or
                    (
                        :conditionLocation = 'IN'
                        and exists (
                            select 1
                            from address_orders ao_in
                            where ao_in.customer_id = c.customer_id
                              and find_in_set(ao_in.address_line, :location) > 0
                        )
                    ) or
                    (
                        :conditionLocation = 'NOT_IN'
                        and not exists (
                            select 1
                            from address_orders ao_not_in
                            where ao_not_in.customer_id = c.customer_id
                              and find_in_set(ao_not_in.address_line, :location) > 0
                        )
                    )
                )
            ) or (
                (:logic = 'OR')
                and (
                    (:totalSpent is not null and (
                        (:conditionSpent = '=' and coalesce(sum(o.total_amount), 0) = :totalSpent) or
                        (:conditionSpent = '>' and coalesce(sum(o.total_amount), 0) > :totalSpent) or
                        (:conditionSpent = '<' and coalesce(sum(o.total_amount), 0) < :totalSpent) or
                        (:conditionSpent = '>=' and coalesce(sum(o.total_amount), 0) >= :totalSpent) or
                        (:conditionSpent = '<=' and coalesce(sum(o.total_amount), 0) <= :totalSpent)
                    ))
                    or (:orderCount is not null and (
                        (:conditionCount = '=' and count(o.order_id) = :orderCount) or
                        (:conditionCount = '>' and count(o.order_id) > :orderCount) or
                        (:conditionCount = '<' and count(o.order_id) < :orderCount) or
                        (:conditionCount = '>=' and count(o.order_id) >= :orderCount) or
                        (:conditionCount = '<=' and count(o.order_id) <= :orderCount)
                    ))
                    or (:lastOrderDate is not null and (
                        (:conditionDate = '=' and date(max(o.created_at)) = :lastOrderDate) or
                        (:conditionDate = '>' and date(max(o.created_at)) > :lastOrderDate) or
                        (:conditionDate = '<' and date(max(o.created_at)) < :lastOrderDate) or
                        (:conditionDate = '>=' and date(max(o.created_at)) >= :lastOrderDate) or
                        (:conditionDate = '<=' and date(max(o.created_at)) <= :lastOrderDate)
                    ))
                    or (:loyaltyTier is not null and (
                        (:conditionTier = 'IN' and find_in_set(l.current_tier, :loyaltyTier) > 0) or
                        (:conditionTier = 'NOT_IN' and find_in_set(l.current_tier, :loyaltyTier) = 0)
                    ))
                    or (:location is not null and (
                        (
                            :conditionLocation = 'IN'
                            and exists (
                                select 1
                                from address_orders ao_in
                                where ao_in.customer_id = c.customer_id
                                  and find_in_set(ao_in.address_line, :location) > 0
                            )
                        ) or
                        (
                            :conditionLocation = 'NOT_IN'
                            and not exists (
                                select 1
                                from address_orders ao_not_in
                                where ao_not_in.customer_id = c.customer_id
                                  and find_in_set(ao_not_in.address_line, :location) > 0
                            )
                        )
                    ))
                )
            )
            order by c.customer_id asc
            """, nativeQuery = true)
    List<SegmentCustomerProjection> findSegmentCustomers(
            @Param("logic") String logic,
            @Param("totalSpent") Double totalSpent,
            @Param("conditionSpent") String conditionSpent,
            @Param("orderCount") Long orderCount,
            @Param("conditionCount") String conditionCount,
            @Param("lastOrderDate") LocalDate lastOrderDate,
            @Param("conditionDate") String conditionDate,
            @Param("loyaltyTier") String loyaltyTier,
            @Param("conditionTier") String conditionTier,
            @Param("location") String location,
            @Param("conditionLocation") String conditionLocation,
            Pageable pageable
    );

    @Query(value = """
            select count(1)
            from (
                select c.customer_id
                from customers c
                left join loyalty l on l.customer_id = c.customer_id
                left join orders o on o.customer_id = c.customer_id
                group by c.customer_id, c.user_id, l.current_tier
                having (
                    (:logic = 'AND')
                    and (
                        :totalSpent is null or
                        (:conditionSpent = '=' and coalesce(sum(o.total_amount), 0) = :totalSpent) or
                        (:conditionSpent = '>' and coalesce(sum(o.total_amount), 0) > :totalSpent) or
                        (:conditionSpent = '<' and coalesce(sum(o.total_amount), 0) < :totalSpent) or
                        (:conditionSpent = '>=' and coalesce(sum(o.total_amount), 0) >= :totalSpent) or
                        (:conditionSpent = '<=' and coalesce(sum(o.total_amount), 0) <= :totalSpent)
                    )
                    and (
                        :orderCount is null or
                        (:conditionCount = '=' and count(o.order_id) = :orderCount) or
                        (:conditionCount = '>' and count(o.order_id) > :orderCount) or
                        (:conditionCount = '<' and count(o.order_id) < :orderCount) or
                        (:conditionCount = '>=' and count(o.order_id) >= :orderCount) or
                        (:conditionCount = '<=' and count(o.order_id) <= :orderCount)
                    )
                    and (
                        :lastOrderDate is null or
                        (:conditionDate = '=' and date(max(o.created_at)) = :lastOrderDate) or
                        (:conditionDate = '>' and date(max(o.created_at)) > :lastOrderDate) or
                        (:conditionDate = '<' and date(max(o.created_at)) < :lastOrderDate) or
                        (:conditionDate = '>=' and date(max(o.created_at)) >= :lastOrderDate) or
                        (:conditionDate = '<=' and date(max(o.created_at)) <= :lastOrderDate)
                    )
                    and (
                        :loyaltyTier is null or
                        (:conditionTier = 'IN' and find_in_set(l.current_tier, :loyaltyTier) > 0) or
                        (:conditionTier = 'NOT_IN' and find_in_set(l.current_tier, :loyaltyTier) = 0)
                    )
                    and (
                        :location is null or
                        (
                            :conditionLocation = 'IN'
                            and exists (
                                select 1
                                from address_orders ao_in
                                where ao_in.customer_id = c.customer_id
                                  and find_in_set(ao_in.address_line, :location) > 0
                            )
                        ) or
                        (
                            :conditionLocation = 'NOT_IN'
                            and not exists (
                                select 1
                                from address_orders ao_not_in
                                where ao_not_in.customer_id = c.customer_id
                                  and find_in_set(ao_not_in.address_line, :location) > 0
                            )
                        )
                    )
                ) or (
                    (:logic = 'OR')
                    and (
                        (:totalSpent is not null and (
                            (:conditionSpent = '=' and coalesce(sum(o.total_amount), 0) = :totalSpent) or
                            (:conditionSpent = '>' and coalesce(sum(o.total_amount), 0) > :totalSpent) or
                            (:conditionSpent = '<' and coalesce(sum(o.total_amount), 0) < :totalSpent) or
                            (:conditionSpent = '>=' and coalesce(sum(o.total_amount), 0) >= :totalSpent) or
                            (:conditionSpent = '<=' and coalesce(sum(o.total_amount), 0) <= :totalSpent)
                        ))
                        or (:orderCount is not null and (
                            (:conditionCount = '=' and count(o.order_id) = :orderCount) or
                            (:conditionCount = '>' and count(o.order_id) > :orderCount) or
                            (:conditionCount = '<' and count(o.order_id) < :orderCount) or
                            (:conditionCount = '>=' and count(o.order_id) >= :orderCount) or
                            (:conditionCount = '<=' and count(o.order_id) <= :orderCount)
                        ))
                        or (:lastOrderDate is not null and (
                            (:conditionDate = '=' and date(max(o.created_at)) = :lastOrderDate) or
                            (:conditionDate = '>' and date(max(o.created_at)) > :lastOrderDate) or
                            (:conditionDate = '<' and date(max(o.created_at)) < :lastOrderDate) or
                            (:conditionDate = '>=' and date(max(o.created_at)) >= :lastOrderDate) or
                            (:conditionDate = '<=' and date(max(o.created_at)) <= :lastOrderDate)
                        ))
                        or (:loyaltyTier is not null and (
                            (:conditionTier = 'IN' and find_in_set(l.current_tier, :loyaltyTier) > 0) or
                            (:conditionTier = 'NOT_IN' and find_in_set(l.current_tier, :loyaltyTier) = 0)
                        ))
                        or (:location is not null and (
                            (
                                :conditionLocation = 'IN'
                                and exists (
                                    select 1
                                    from address_orders ao_in
                                    where ao_in.customer_id = c.customer_id
                                      and find_in_set(ao_in.address_line, :location) > 0
                                )
                            ) or
                            (
                                :conditionLocation = 'NOT_IN'
                                and not exists (
                                    select 1
                                    from address_orders ao_not_in
                                    where ao_not_in.customer_id = c.customer_id
                                      and find_in_set(ao_not_in.address_line, :location) > 0
                                )
                            )
                        ))
                    )
                )
            ) x
            """, nativeQuery = true)
    long countSegmentCustomers(
            @Param("logic") String logic,
            @Param("totalSpent") Double totalSpent,
            @Param("conditionSpent") String conditionSpent,
            @Param("orderCount") Long orderCount,
            @Param("conditionCount") String conditionCount,
            @Param("lastOrderDate") LocalDate lastOrderDate,
            @Param("conditionDate") String conditionDate,
            @Param("loyaltyTier") String loyaltyTier,
            @Param("conditionTier") String conditionTier,
            @Param("location") String location,
            @Param("conditionLocation") String conditionLocation
    );

    @Query(value = """
    select 1
    from customers c
    left join loyalty l on l.customer_id = c.customer_id
    left join orders o on o.customer_id = c.customer_id
    where c.customer_id = :customerId
    group by c.customer_id, c.user_id, l.current_tier
            having (
        (:logic = 'AND')
        and (
            :totalSpent is null or
            (:conditionSpent = '=' and coalesce(sum(o.total_amount), 0) = :totalSpent) or
            (:conditionSpent = '>' and coalesce(sum(o.total_amount), 0) > :totalSpent) or
            (:conditionSpent = '<' and coalesce(sum(o.total_amount), 0) < :totalSpent) or
            (:conditionSpent = '>=' and coalesce(sum(o.total_amount), 0) >= :totalSpent) or
            (:conditionSpent = '<=' and coalesce(sum(o.total_amount), 0) <= :totalSpent)
        )
        and (
            :orderCount is null or
            (:conditionCount = '=' and count(o.order_id) = :orderCount) or
            (:conditionCount = '>' and count(o.order_id) > :orderCount) or
            (:conditionCount = '<' and count(o.order_id) < :orderCount) or
            (:conditionCount = '>=' and count(o.order_id) >= :orderCount) or
            (:conditionCount = '<=' and count(o.order_id) <= :orderCount)
        )
        and (
            :lastOrderDate is null or
            (:conditionDate = '=' and date(max(o.created_at)) = :lastOrderDate) or
            (:conditionDate = '>' and date(max(o.created_at)) > :lastOrderDate) or
            (:conditionDate = '<' and date(max(o.created_at)) < :lastOrderDate) or
            (:conditionDate = '>=' and date(max(o.created_at)) >= :lastOrderDate) or
            (:conditionDate = '<=' and date(max(o.created_at)) <= :lastOrderDate)
        )
        and (
            :loyaltyTier is null or
            (:conditionTier = 'IN' and find_in_set(l.current_tier, :loyaltyTier) > 0) or
            (:conditionTier = 'NOT_IN' and find_in_set(l.current_tier, :loyaltyTier) = 0)
        )
        and (
            :location is null or
            (
                :conditionLocation = 'IN'
                and exists (
                    select 1
                    from address_orders ao_in
                    where ao_in.customer_id = c.customer_id
                      and find_in_set(ao_in.address_line, :location) > 0
                )
            ) or
            (
                :conditionLocation = 'NOT_IN'
                and not exists (
                    select 1
                    from address_orders ao_not_in
                    where ao_not_in.customer_id = c.customer_id
                      and find_in_set(ao_not_in.address_line, :location) > 0
                )
            )
        )
            )
            or (
        (:logic = 'OR')
        and (
            (:totalSpent is not null and (
                (:conditionSpent = '=' and coalesce(sum(o.total_amount), 0) = :totalSpent) or
                (:conditionSpent = '>' and coalesce(sum(o.total_amount), 0) > :totalSpent) or
                (:conditionSpent = '<' and coalesce(sum(o.total_amount), 0) < :totalSpent) or
                (:conditionSpent = '>=' and coalesce(sum(o.total_amount), 0) >= :totalSpent) or
                (:conditionSpent = '<=' and coalesce(sum(o.total_amount), 0) <= :totalSpent)
            ))
            or (:orderCount is not null and (
                (:conditionCount = '=' and count(o.order_id) = :orderCount) or
                (:conditionCount = '>' and count(o.order_id) > :orderCount) or
                (:conditionCount = '<' and count(o.order_id) < :orderCount) or
                (:conditionCount = '>=' and count(o.order_id) >= :orderCount) or
                (:conditionCount = '<=' and count(o.order_id) <= :orderCount)
            ))
            or (:lastOrderDate is not null and (
                (:conditionDate = '=' and date(max(o.created_at)) = :lastOrderDate) or
                (:conditionDate = '>' and date(max(o.created_at)) > :lastOrderDate) or
                (:conditionDate = '<' and date(max(o.created_at)) < :lastOrderDate) or
                (:conditionDate = '>=' and date(max(o.created_at)) >= :lastOrderDate) or
                (:conditionDate = '<=' and date(max(o.created_at)) <= :lastOrderDate)
            ))
            or (:loyaltyTier is not null and (
                (:conditionTier = 'IN' and find_in_set(l.current_tier, :loyaltyTier) > 0) or
                (:conditionTier = 'NOT_IN' and find_in_set(l.current_tier, :loyaltyTier) = 0)
            ))
            or (:location is not null and (
                (
                    :conditionLocation = 'IN'
                    and exists (
                        select 1
                        from address_orders ao_in
                        where ao_in.customer_id = c.customer_id
                          and find_in_set(ao_in.address_line, :location) > 0
                    )
                ) or
                (
                    :conditionLocation = 'NOT_IN'
                    and not exists (
                        select 1
                        from address_orders ao_not_in
                        where ao_not_in.customer_id = c.customer_id
                          and find_in_set(ao_not_in.address_line, :location) > 0
                    )
                )
            ))
        )
            )
    limit 1
    """, nativeQuery = true)
    Integer existsCustomerInSegment(
            @Param("customerId") Long customerId,
            @Param("logic") String logic,
            @Param("totalSpent") Double totalSpent,
            @Param("conditionSpent") String conditionSpent,
            @Param("orderCount") Long orderCount,
            @Param("conditionCount") String conditionCount,
            @Param("lastOrderDate") LocalDate lastOrderDate,
            @Param("conditionDate") String conditionDate,
            @Param("loyaltyTier") String loyaltyTier,
            @Param("conditionTier") String conditionTier,
            @Param("location") String location,
            @Param("conditionLocation") String conditionLocation
    );

    /**
     * Cùng điều kiện {@link #countSegmentCustomers} nhưng chỉ đánh giá một khách — dùng cho membership (promotion).
     */
    @Query(value = """
            select count(1)
            from (
                select c.customer_id
                from customers c
                left join loyalty l on l.customer_id = c.customer_id
                left join orders o on o.customer_id = c.customer_id
                where c.customer_id = :customerId
                group by c.customer_id, c.user_id, l.current_tier
                having (
                    (:logic = 'AND')
                    and (
                        :totalSpent is null or
                        (:conditionSpent = '=' and coalesce(sum(o.total_amount), 0) = :totalSpent) or
                        (:conditionSpent = '>' and coalesce(sum(o.total_amount), 0) > :totalSpent) or
                        (:conditionSpent = '<' and coalesce(sum(o.total_amount), 0) < :totalSpent) or
                        (:conditionSpent = '>=' and coalesce(sum(o.total_amount), 0) >= :totalSpent) or
                        (:conditionSpent = '<=' and coalesce(sum(o.total_amount), 0) <= :totalSpent)
                    )
                    and (
                        :orderCount is null or
                        (:conditionCount = '=' and count(o.order_id) = :orderCount) or
                        (:conditionCount = '>' and count(o.order_id) > :orderCount) or
                        (:conditionCount = '<' and count(o.order_id) < :orderCount) or
                        (:conditionCount = '>=' and count(o.order_id) >= :orderCount) or
                        (:conditionCount = '<=' and count(o.order_id) <= :orderCount)
                    )
                    and (
                        :lastOrderDate is null or
                        (:conditionDate = '=' and date(max(o.created_at)) = :lastOrderDate) or
                        (:conditionDate = '>' and date(max(o.created_at)) > :lastOrderDate) or
                        (:conditionDate = '<' and date(max(o.created_at)) < :lastOrderDate) or
                        (:conditionDate = '>=' and date(max(o.created_at)) >= :lastOrderDate) or
                        (:conditionDate = '<=' and date(max(o.created_at)) <= :lastOrderDate)
                    )
                    and (
                        :loyaltyTier is null or
                        (:conditionTier = 'IN' and find_in_set(l.current_tier, :loyaltyTier) > 0) or
                        (:conditionTier = 'NOT_IN' and find_in_set(l.current_tier, :loyaltyTier) = 0)
                    )
                    and (
                        :location is null or
                        (
                            :conditionLocation = 'IN'
                            and exists (
                                select 1
                                from address_orders ao_in
                                where ao_in.customer_id = c.customer_id
                                  and find_in_set(ao_in.address_line, :location) > 0
                            )
                        ) or
                        (
                            :conditionLocation = 'NOT_IN'
                            and not exists (
                                select 1
                                from address_orders ao_not_in
                                where ao_not_in.customer_id = c.customer_id
                                  and find_in_set(ao_not_in.address_line, :location) > 0
                            )
                        )
                    )
                ) or (
                    (:logic = 'OR')
                    and (
                        (:totalSpent is not null and (
                            (:conditionSpent = '=' and coalesce(sum(o.total_amount), 0) = :totalSpent) or
                            (:conditionSpent = '>' and coalesce(sum(o.total_amount), 0) > :totalSpent) or
                            (:conditionSpent = '<' and coalesce(sum(o.total_amount), 0) < :totalSpent) or
                            (:conditionSpent = '>=' and coalesce(sum(o.total_amount), 0) >= :totalSpent) or
                            (:conditionSpent = '<=' and coalesce(sum(o.total_amount), 0) <= :totalSpent)
                        ))
                        or (:orderCount is not null and (
                            (:conditionCount = '=' and count(o.order_id) = :orderCount) or
                            (:conditionCount = '>' and count(o.order_id) > :orderCount) or
                            (:conditionCount = '<' and count(o.order_id) < :orderCount) or
                            (:conditionCount = '>=' and count(o.order_id) >= :orderCount) or
                            (:conditionCount = '<=' and count(o.order_id) <= :orderCount)
                        ))
                        or (:lastOrderDate is not null and (
                            (:conditionDate = '=' and date(max(o.created_at)) = :lastOrderDate) or
                            (:conditionDate = '>' and date(max(o.created_at)) > :lastOrderDate) or
                            (:conditionDate = '<' and date(max(o.created_at)) < :lastOrderDate) or
                            (:conditionDate = '>=' and date(max(o.created_at)) >= :lastOrderDate) or
                            (:conditionDate = '<=' and date(max(o.created_at)) <= :lastOrderDate)
                        ))
                        or (:loyaltyTier is not null and (
                            (:conditionTier = 'IN' and find_in_set(l.current_tier, :loyaltyTier) > 0) or
                            (:conditionTier = 'NOT_IN' and find_in_set(l.current_tier, :loyaltyTier) = 0)
                        ))
                        or (:location is not null and (
                            (
                                :conditionLocation = 'IN'
                                and exists (
                                    select 1
                                    from address_orders ao_in
                                    where ao_in.customer_id = c.customer_id
                                      and find_in_set(ao_in.address_line, :location) > 0
                                )
                            ) or
                            (
                                :conditionLocation = 'NOT_IN'
                                and not exists (
                                    select 1
                                    from address_orders ao_not_in
                                    where ao_not_in.customer_id = c.customer_id
                                      and find_in_set(ao_not_in.address_line, :location) > 0
                                )
                            )
                        ))
                    )
                )
            ) x
            """, nativeQuery = true)
    long countSegmentCustomersForCustomer(
            @Param("customerId") Long customerId,
            @Param("logic") String logic,
            @Param("totalSpent") Double totalSpent,
            @Param("conditionSpent") String conditionSpent,
            @Param("orderCount") Long orderCount,
            @Param("conditionCount") String conditionCount,
            @Param("lastOrderDate") LocalDate lastOrderDate,
            @Param("conditionDate") String conditionDate,
            @Param("loyaltyTier") String loyaltyTier,
            @Param("conditionTier") String conditionTier,
            @Param("location") String location,
            @Param("conditionLocation") String conditionLocation
    );
}