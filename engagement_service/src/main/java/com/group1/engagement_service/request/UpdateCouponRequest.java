/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.group1.engagement_service.request;

import com.group1.engagement_service.entity.CouponStatus;
import lombok.Data;

@Data
public class UpdateCouponRequest {
    private String code;
    private Integer maxUses;
    private CouponStatus status;
}