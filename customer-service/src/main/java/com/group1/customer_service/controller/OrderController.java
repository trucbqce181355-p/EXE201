package com.group1.customer_service.controller;

import com.group1.customer_service.dto.request.CreateOrderRequest;
import com.group1.customer_service.dto.response.ApiResponse;
import com.group1.customer_service.dto.response.OrderDetailResponse;
import com.group1.customer_service.dto.response.OrderResponse;
import com.group1.customer_service.dto.response.PageResponse;
import com.group1.customer_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/customer/{customerId}/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDetailResponse>> createOrder(
            @PathVariable Long customerId,
            @Valid @RequestBody CreateOrderRequest request) {
        
        OrderDetailResponse data = orderService.createOrder(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Order placed successfully", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getOrders(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        PageResponse<OrderResponse> data = orderService.getOrders(customerId, page, limit, status, fromDate, toDate);
        return ResponseEntity.ok(new ApiResponse<>(true, "Orders fetched successfully", data));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
            @PathVariable Long customerId,
            @PathVariable Long orderId) {

        OrderDetailResponse data = orderService.getOrderDetail(customerId, orderId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Order details fetched successfully", data));
    }
}
