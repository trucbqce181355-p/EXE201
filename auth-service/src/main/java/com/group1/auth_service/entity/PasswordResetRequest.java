package com.group1.auth_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @PrePersist
    void prePersist() {
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
    }
}
