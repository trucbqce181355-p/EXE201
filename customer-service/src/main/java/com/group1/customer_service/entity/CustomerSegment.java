package com.group1.customer_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "customer_segments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "logic_operator", nullable = false, length = 5)
    private String logicOperator;

    @Column(name = "total_spent")
    private Double totalSpent;

    @Column(name = "condition_spent", length = 5)
    private String conditionSpent;

    @Column(name = "order_count")
    private Integer orderCount;

    @Column(name = "condition_count", length = 5)
    private String conditionCount;

    @Column(name = "last_order_date")
    private LocalDate lastOrderDate;

    @Column(name = "condition_date", length = 5)
    private String conditionDate;

    @Column(name = "loyalty_tier", length = 30)
    private String loyaltyTier;

    @Column(name = "condition_tier", length = 10)
    private String conditionTier;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "condition_location", length = 10)
    private String conditionLocation;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}

