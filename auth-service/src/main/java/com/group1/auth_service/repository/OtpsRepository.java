package com.group1.auth_service.repository;

import com.group1.auth_service.entity.Otps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpsRepository extends JpaRepository<Otps, Long> {
    Optional<Otps> findByEmail(String email);
}
