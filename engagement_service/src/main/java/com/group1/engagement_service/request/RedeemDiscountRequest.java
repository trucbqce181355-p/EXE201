/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.group1.engagement_service.request;

import lombok.Data;

@Data
public class RedeemDiscountRequest {
    private String type; // CHECKOUT_DISCOUNT
    private Integer points;
    private Long orderId;
}