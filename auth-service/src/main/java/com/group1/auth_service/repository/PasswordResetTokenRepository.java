package com.group1.auth_service.repository;

import com.group1.auth_service.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    Optional<PasswordResetToken> findByTokenHashAndUsedFalse(String tokenHash);
    Optional<PasswordResetToken> findByTokenHashAndUser_IdAndUsedFalse(String tokenHash, Long userId);
    Optional<PasswordResetToken> findByTokenHashAndUser_Id(String tokenHash, Long userId);
    List<PasswordResetToken> findAllByUser_IdAndUsedFalse(Long userId);
    void deleteByUser_Id(Long userId);
}
