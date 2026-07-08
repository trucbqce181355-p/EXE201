/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.group1.auth_service.service;

import com.group1.auth_service.entity.RevokedToken;
import com.group1.auth_service.repository.RevokedTokenRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    private final RevokedTokenRepository repository;

    public TokenBlacklistService(RevokedTokenRepository repository) {
        this.repository = repository;
    }

    public void blacklist(String token, LocalDateTime expiryDate) {
        System.out.println("Saving token to DB...");
        RevokedToken revokedToken = new RevokedToken();
        revokedToken.setToken(token);
        revokedToken.setExpiryDate(expiryDate);
        repository.save(revokedToken);
    }

    public boolean isBlacklisted(String token) {
        return repository.existsByToken(token);
    }
}
