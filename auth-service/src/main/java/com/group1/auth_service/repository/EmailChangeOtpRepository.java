package com.group1.auth_service.repository;

import com.group1.auth_service.entity.EmailChangeOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailChangeOtpRepository extends JpaRepository<EmailChangeOtp, Long> {
    Optional<EmailChangeOtp> findFirstByUser_IdAndNewEmailAndUsedFalseOrderByCreatedAtDesc(Long userId, String newEmail);
    void deleteByUser_Id(Long userId);
}
