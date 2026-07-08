/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.group1.auth_service.repository;

import com.group1.auth_service.entity.RevokedToken;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RevokedTokenRepository 
        extends JpaRepository<RevokedToken, Long> {

    boolean existsByToken(String token);
    
    @Transactional
    @Modifying
    long deleteByExpiryDateBefore(LocalDateTime now);
}
