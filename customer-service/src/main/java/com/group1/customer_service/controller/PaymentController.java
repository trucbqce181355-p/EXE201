package com.group1.customer_service.controller;

import com.group1.customer_service.dto.response.ApiResponse;
import com.group1.customer_service.entity.Order;
import com.group1.customer_service.repository.OrderRepository;
import com.group1.customer_service.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment/vnpay")
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayService vnPayService;
    private final OrderRepository orderRepository;

    @GetMapping("/create-url")
    public ResponseEntity<ApiResponse<String>> createPaymentUrl(@RequestParam Long orderId, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        if (ipAddress.equals("0:0:0:0:0:0:0:1")) {
            ipAddress = "127.0.0.1";
        }
        String paymentUrl = vnPayService.createPaymentUrl(orderId, ipAddress);
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", paymentUrl));
    }

    @GetMapping("/return")
    public ResponseEntity<ApiResponse<String>> paymentReturn(@RequestParam Map<String, String> params) {
        boolean isValidSignature = vnPayService.verifySignature(params);
        if (!isValidSignature) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid signature", null));
        }

        String orderNumber = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if ("00".equals(responseCode)) {
            // Payment success
            if (order.getPayment() != null && !"PAID".equals(order.getPayment().getStatus())) {
                order.getPayment().setStatus("PAID");
                order.getPayment().setPaidAt(LocalDateTime.now());
                order.setStatus("Processing");
                orderRepository.save(order);
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment successful", orderNumber));
        } else {
            // Payment failed
            if (order.getPayment() != null) {
                order.getPayment().setStatus("FAILED");
                orderRepository.save(order);
            }
            return ResponseEntity.ok(new ApiResponse<>(false, "Payment failed", orderNumber));
        }
    }
}
