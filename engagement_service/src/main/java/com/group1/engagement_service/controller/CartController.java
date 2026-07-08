/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.group1.engagement_service.controller;

import com.group1.engagement_service.dto.CartDTO;
import com.group1.engagement_service.entity.Cart;
import com.group1.engagement_service.request.ApplyCouponRequest;
import com.group1.engagement_service.response.CartResponse;
import com.group1.engagement_service.security.AuthenticatedUser;
import com.group1.engagement_service.service.CartService;
import java.nio.file.attribute.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/cart"})
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<CartDTO> getCart(@PathVariable Long customerId) {
        // Hàm này trong Service giờ đã trả về CartDTO rồi
        CartDTO cartDto = cartService.getCartByCustomerId(customerId);

        return ResponseEntity.ok(cartDto);
    }

    @PutMapping("/item/{itemId}/quantity")
    public ResponseEntity<CartDTO> updateItemQuantity(
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {

        CartDTO updatedCart = cartService.updateItemQuantity(itemId, quantity);
        return ResponseEntity.ok(updatedCart);
    }

    @PostMapping("/apply-coupon")
    public ResponseEntity<CartDTO> applyCoupon(
            @RequestHeader("Authorization") String token,
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody ApplyCouponRequest request) {
        System.out.println("coupn-code:" + request.getCouponCode());
        return ResponseEntity.ok(cartService.applyCoupon(user.getUserId(), request, token));
    }

    @DeleteMapping("/coupon")
    public ResponseEntity<?> removeCoupon(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestHeader("Authorization") String token,
            @RequestParam String couponCode) { // Thêm cái này để biết gỡ mã nào

        return ResponseEntity.ok(cartService.removeSpecificCoupon(user.getUserId(), couponCode, token));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<CartDTO> clearCart(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(cartService.clearCart(user.getUserId(), token));
    }

    @PostMapping("/item")
    public ResponseEntity<CartDTO> addItem(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestHeader("Authorization") String token,
            @RequestParam Long productId,
            @RequestParam String productName,
            @RequestParam java.math.BigDecimal price,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.addItemToCart(user.getUserId(), productId, productName, price, quantity, token));
    }
}
