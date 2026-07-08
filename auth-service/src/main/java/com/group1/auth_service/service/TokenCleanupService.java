/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.group1.auth_service.service;

import com.group1.auth_service.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final RevokedTokenRepository repository;

    @Transactional
    @Scheduled(fixedRate = 600000)
    public void removeExpiredTokens() {
        long deletedCount = repository.deleteByExpiryDateBefore(LocalDateTime.now());
        log.debug("Expired tokens cleaned: {}", deletedCount);
    }
}
