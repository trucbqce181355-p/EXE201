/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.group1.engagement_service.repository;

import com.group1.engagement_service.entity.LoyaltyRedemption;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface LoyaltyRedemptionRepository extends JpaRepository<LoyaltyRedemption, Long>{
    List<LoyaltyRedemption> findByCustomerId(Long customerId);
    
}
