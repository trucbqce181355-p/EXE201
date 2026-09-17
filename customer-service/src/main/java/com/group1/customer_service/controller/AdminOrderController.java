package com.group1.customer_service.controller;

import com.group1.customer_service.dto.request.UpdateOrderStatusRequest;
import com.group1.customer_service.dto.response.AdminOrderResponse;
import com.group1.customer_service.dto.response.ApiResponse;
import com.group1.customer_service.dto.response.OrderDetailResponse;
import com.group1.customer_service.dto.response.PageResponse;
import com.group1.customer_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminOrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        PageResponse<AdminOrderResponse> data = orderService.getAllOrdersAdmin(page, limit, status, fromDate, toDate);
        return ResponseEntity.ok(new ApiResponse<>(true, "All orders fetched successfully", data));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        OrderDetailResponse data = orderService.adminUpdateOrderStatus(orderId, request.getNewStatus(), request.getNote());
        return ResponseEntity.ok(new ApiResponse<>(true, "Order status updated successfully by Admin", data));
    }
}
