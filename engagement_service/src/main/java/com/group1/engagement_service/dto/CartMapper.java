/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.group1.engagement_service.dto;

import com.group1.engagement_service.entity.Cart;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CartMapper {

    public CartDTO toDTO(Cart cart) {
        if (cart == null) {
            return null;
        }

        List<CartItemDTO> itemDTOs = cart.getItems().stream()
                .map(item -> CartItemDTO.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .build())
                .collect(Collectors.toList());

        return CartDTO.builder()
                .id(cart.getId())
                .customerId(cart.getCustomerId())
                .totalAmount(cart.getTotalAmount())
                .discountAmount(cart.getDiscountAmount())
                .finalAmount(cart.getFinalAmount())
                .items(itemDTOs)
                .appliedCouponCodes(cart.getAppliedCoupons().stream()
                        .map(cc -> cc.getCoupon().getCode())
                        .toList())
                .build();
    }
}
