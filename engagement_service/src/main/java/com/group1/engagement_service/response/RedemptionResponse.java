/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.group1.engagement_service.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RedemptionResponse {
    private Long id;
    private Integer pointsUsed;
    private String status;
    private String type;
    private LocalDateTime createdAt;
    private String rewardDescription;
}