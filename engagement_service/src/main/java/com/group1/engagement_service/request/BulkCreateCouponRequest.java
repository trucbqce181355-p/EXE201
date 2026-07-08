/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.group1.engagement_service.request;

import lombok.Data;

@Data
public class BulkCreateCouponRequest {
    private Long promotionId;
    private Integer quantity;
    private String prefix;
    private Integer maxUsesPerCode;
}
