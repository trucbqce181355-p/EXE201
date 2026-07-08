/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.group1.customer_service.dto.response;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class DeliveryInfoResponse {
    private String address;
    private String receiverName;
    private String phone;
    private String status;
}