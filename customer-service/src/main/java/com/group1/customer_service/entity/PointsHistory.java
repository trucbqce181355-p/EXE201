package com.group1.customer_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "points_history")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PointsHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointType type;

    @Column(nullable = false)
    private Integer amount;

    private String description;

    private String orderId;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}