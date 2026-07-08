package com.group1.auth_service.repository;

import com.group1.auth_service.entity.PasswordResetRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequest, Long> {
    long countByEmailAndRequestedAtAfter(String email, LocalDateTime requestedAt);
}
