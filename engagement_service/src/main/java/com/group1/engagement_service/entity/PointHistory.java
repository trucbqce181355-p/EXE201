package com.group1.engagement_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private Integer amount;

    @Column(length = 255, nullable = false)
    private String reason;

    @Column(name = "adjusted_by")
    private String adjustedBy;

    // ✅ giữ Long
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}