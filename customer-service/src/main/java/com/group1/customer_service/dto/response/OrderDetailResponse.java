package com.group1.customer_service.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

@Data
@Builder
public class OrderDetailResponse {

    private Long orderId;
   

    private List<OrderItemResponse> items;
    private PaymentInfoResponse paymentInfo;
    private DeliveryInfoResponse deliveryInfo;
    private List<StatusHistoryResponse> statusHistory;
}
