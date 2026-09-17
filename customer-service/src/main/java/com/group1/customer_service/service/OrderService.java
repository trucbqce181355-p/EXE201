/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.group1.customer_service.service;

import com.group1.customer_service.dto.request.CreateOrderRequest;

import com.group1.customer_service.dto.response.DeliveryInfoResponse;
import com.group1.customer_service.dto.response.OrderDetailResponse;
import com.group1.customer_service.dto.response.OrderItemResponse;
import com.group1.customer_service.dto.response.OrderResponse;
import com.group1.customer_service.dto.response.PageResponse;
import com.group1.customer_service.dto.response.PaymentInfoResponse;
import com.group1.customer_service.dto.response.StatusHistoryResponse;
import com.group1.customer_service.entity.Customer;
import com.group1.customer_service.entity.Order;
import com.group1.customer_service.entity.OrderDelivery;
import com.group1.customer_service.entity.OrderItem;
import com.group1.customer_service.entity.OrderPayment;
import com.group1.customer_service.entity.OrderStatusHistory;
import com.group1.customer_service.repository.CustomerRepository;
import com.group1.customer_service.repository.OrderRepository;
import java.math.BigDecimal;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final com.group1.customer_service.repository.AddressOrderRepository addressOrderRepository;

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrders(
            Long customerId,
            int page,
            int limit,
            List<String> status,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        try {
            System.out.println("[OrderService] Listing orders for customerId: " + customerId + ", status: " + status);
            Pageable pageable = PageRequest.of(
                    page - 1,
                    limit,
                    Sort.by("createdAt").descending()
            );

            // Avoid JPA collection parameter null/empty query issue
            List<String> statusList = (status != null && !status.isEmpty()) ? status : null;

            Page<Order> orders = orderRepository.filterOrders(
                    customerId,
                    statusList,
                    fromDate != null ? fromDate.atStartOfDay() : null,
                    toDate != null ? toDate.atTime(23, 59, 59) : null,
                    pageable
            );

            List<OrderResponse> data = orders.getContent().stream()
                    .map(o -> new OrderResponse(
                    o.getOrderId(),
                    o.getOrderNumber(),
                    o.getStatus(),
                    o.getTotalAmount(),
                    o.getCreatedAt(),
                    o.getItems() != null ? o.getItems().size() : 0
            ))
                    .toList();

            return new PageResponse<>(
                    data,
                    orders.getTotalElements(),
                    page,
                    limit
            );
        } catch (Exception e) {
            System.out.println("[OrderService] Error listing orders: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi hiển thị danh sách đơn hàng: " + e.getMessage(), e);
        }
    }

    public OrderDetailResponse getOrderDetail(Long customerId, Long orderId) {
        Order order = orderRepository.findOrderDetail(orderId, customerId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return OrderDetailResponse.builder()
                .orderId(order.getOrderId())
                .items(order.getItems() != null 
                        ? order.getItems().stream()
                                .map(i -> OrderItemResponse.builder()
                                .productName(i.getProduct())
                                .quantity(i.getQuantity())
                                .price(i.getPrice())
                                .build())
                                .toList()
                        : java.util.Collections.emptyList())
                .paymentInfo(order.getPayment() != null
                        ? PaymentInfoResponse.builder()
                                .method(order.getPayment().getMethod())
                                .status(order.getPayment().getStatus())
                                .paidAt(order.getPayment().getPaidAt())
                                .build()
                        : null)
                .deliveryInfo(order.getDelivery() != null
                        ? DeliveryInfoResponse.builder()
                                .address(order.getDelivery().getAddress())
                                .receiverName(order.getDelivery().getReceiverName())
                                .phone(order.getDelivery().getPhone())
                                .status(order.getDelivery().getStatus())
                                .build()
                        : null)
                .statusHistory(order.getStatusHistories() != null
                        ? order.getStatusHistories().stream()
                                .map(s -> StatusHistoryResponse.builder()
                                .status(s.getStatus())
                                .updatedAt(s.getUpdatedAt())
                                .build())
                                .toList()
                        : java.util.Collections.emptyList())
                .build();
    }

    @Transactional
    public void applyDiscount(Long orderId, BigDecimal discountAmount) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // ❌ Không cho discount nếu order đã complete/cancel
        if (order.getStatus().equals("Completed") || order.getStatus().equals("Cancelled")) {
            throw new RuntimeException("Cannot apply discount to this order");
        }

        // ❌ tránh apply 2 lần
        if (order.getDiscountAmount() != null && order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Discount already applied");
        }

        // ✅ set discount
        order.setDiscountAmount(discountAmount);

        // ✅ tính lại tiền
        BigDecimal finalAmount = order.getTotalAmount().subtract(discountAmount);

        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        order.setFinalAmount(finalAmount);

        orderRepository.save(order);
    }

    @Transactional
    public OrderDetailResponse createOrder(Long customerId, CreateOrderRequest request) {
        try {
            System.out.println("[OrderService] Placing order for customerId: " + customerId);
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            // Auto-save new address to profile if it doesn't exist
            if (request.getAddress() != null && !request.getAddress().trim().isEmpty()) {
                String inputAddress = request.getAddress().trim();
                boolean addressExists = false;
                if (customer.getAddressOrders() != null) {
                    for (com.group1.customer_service.entity.AddressOrder addr : customer.getAddressOrders()) {
                        if (inputAddress.equalsIgnoreCase(addr.getAddressLine())) {
                            addressExists = true;
                            break;
                        }
                    }
                }
                
                if (!addressExists) {
                    com.group1.customer_service.entity.AddressOrder newAddress = com.group1.customer_service.entity.AddressOrder.builder()
                            .customer(customer)
                            .addressLine(inputAddress)
                            .isDefault(customer.getAddressOrders() == null || customer.getAddressOrders().isEmpty())
                            .build();
                    addressOrderRepository.save(newAddress);
                }
            }

            BigDecimal totalAmount = BigDecimal.ZERO;
            for (var reqItem : request.getItems()) {
                BigDecimal itemTotal = reqItem.getPrice().multiply(BigDecimal.valueOf(reqItem.getQuantity()));
                totalAmount = totalAmount.add(itemTotal);
            }

            BigDecimal discountAmount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal finalAmount = totalAmount.subtract(discountAmount);
            if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                finalAmount = BigDecimal.ZERO;
            }

            String orderNumber = "ORD-" + System.currentTimeMillis();

            Order order = Order.builder()
                    .customer(customer)
                    .orderNumber(orderNumber)
                    .status("Pending")
                    .totalAmount(totalAmount)
                    .discountAmount(discountAmount)
                    .finalAmount(finalAmount)
                    .createdAt(LocalDateTime.now())
                    .build();

            Order savedOrder = orderRepository.save(order);

            // Save Items
            java.util.Set<OrderItem> items = new java.util.LinkedHashSet<>();
            for (var reqItem : request.getItems()) {
                OrderItem item = OrderItem.builder()
                        .order(savedOrder)
                        .product(reqItem.getProduct())
                        .quantity(reqItem.getQuantity())
                        .price(reqItem.getPrice())
                        .build();
                items.add(item);
            }
            savedOrder.setItems(items);

            // Save Payment
            OrderPayment payment = OrderPayment.builder()
                    .order(savedOrder)
                    .method(request.getPaymentMethod())
                    .status("PENDING")
                    .build();
            savedOrder.setPayment(payment);

            // Save Delivery
            OrderDelivery delivery = OrderDelivery.builder()
                    .order(savedOrder)
                    .address(request.getAddress())
                    .receiverName(request.getReceiverName())
                    .phone(request.getPhone())
                    .status("PENDING")
                    .build();
            savedOrder.setDelivery(delivery);

            // Save Status History
            OrderStatusHistory history = OrderStatusHistory.builder()
                    .order(savedOrder)
                    .status("Pending")
                    .updatedAt(LocalDateTime.now())
                    .build();
            java.util.Set<OrderStatusHistory> histories = new java.util.LinkedHashSet<>();
            histories.add(history);
            savedOrder.setStatusHistories(histories);

            orderRepository.save(savedOrder);
            System.out.println("[OrderService] Order placed successfully. OrderID: " + savedOrder.getOrderId());

            return getOrderDetail(customerId, savedOrder.getOrderId());
        } catch (Exception e) {
            System.out.println("[OrderService] Error placing order: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi đặt hàng ở hệ thống: " + e.getMessage(), e);
        }
    }

    @Transactional
    public OrderDetailResponse updateOrderStatus(Long customerId, Long orderId, String newStatus, String note) {
        Order order = orderRepository.findOrderDetail(orderId, customerId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus().equals("Cancelled")) {
            throw new RuntimeException("Cannot update status of a cancelled order");
        }

        order.setStatus(newStatus);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(newStatus)
                .updatedAt(LocalDateTime.now())
                .build();
        
        if (order.getStatusHistories() != null) {
            order.getStatusHistories().add(history);
        } else {
            java.util.Set<OrderStatusHistory> histories = new java.util.LinkedHashSet<>();
            histories.add(history);
            order.setStatusHistories(histories);
        }

        orderRepository.save(order);
        return getOrderDetail(customerId, orderId);
    }

    @Transactional
    public OrderDetailResponse cancelOrder(Long customerId, Long orderId, String reason) {
        Order order = orderRepository.findOrderDetail(orderId, customerId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus().equals("Completed") || order.getStatus().equals("Shipped") || order.getStatus().equals("Cancelled")) {
            throw new RuntimeException("Order cannot be cancelled in its current status: " + order.getStatus());
        }

        order.setStatus("Cancelled");

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status("Cancelled")
                .updatedAt(LocalDateTime.now())
                .build();
        
        if (order.getStatusHistories() != null) {
            order.getStatusHistories().add(history);
        } else {
            java.util.Set<OrderStatusHistory> histories = new java.util.LinkedHashSet<>();
            histories.add(history);
            order.setStatusHistories(histories);
        }

        orderRepository.save(order);
        return getOrderDetail(customerId, orderId);
    }

    @Transactional
    public OrderDetailResponse processPayment(Long customerId, Long orderId) {
        Order order = orderRepository.findOrderDetail(orderId, customerId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderPayment payment = order.getPayment();
        if (payment == null) {
            throw new RuntimeException("No payment record found for this order");
        }

        if (payment.getStatus().equals("PAID")) {
            throw new RuntimeException("Order is already paid");
        }

        payment.setStatus("PAID");
        payment.setPaidAt(LocalDateTime.now());

        orderRepository.save(order);
        return getOrderDetail(customerId, orderId);
    }

    @Transactional(readOnly = true)
    public PageResponse<com.group1.customer_service.dto.response.AdminOrderResponse> getAllOrdersAdmin(
            int page, int limit, List<String> status, LocalDate fromDate, LocalDate toDate) {
        try {
            Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
            List<String> statusList = (status != null && !status.isEmpty()) ? status : null;

            Page<Order> orders;
            
            if (statusList == null && fromDate == null && toDate == null) {
                orders = orderRepository.findAll(pageable);
            } else {
                orders = orderRepository.filterAllOrders(
                        statusList,
                        fromDate != null ? fromDate.atStartOfDay() : null,
                        toDate != null ? toDate.atTime(23, 59, 59) : null,
                        pageable
                );
            }

            List<com.group1.customer_service.dto.response.AdminOrderResponse> data = orders.getContent().stream()
                    .map(o -> com.group1.customer_service.dto.response.AdminOrderResponse.builder()
                            .orderId(o.getOrderId())
                            .orderNumber(o.getOrderNumber())
                            .status(o.getStatus())
                            .totalAmount(o.getTotalAmount())
                            .createdAt(o.getCreatedAt())
                            .itemsCount(o.getItems() != null ? o.getItems().size() : 0)
                            .customerId(o.getCustomer().getCustomerId())
                            .customerName(o.getCustomer().getCustomerCode() != null ? o.getCustomer().getCustomerCode() : "Khách hàng")
                            .paymentStatus(o.getPayment() != null ? o.getPayment().getStatus() : "UNKNOWN")
                            .build())
                    .toList();

            return new PageResponse<>(data, orders.getTotalElements(), page, limit);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi hiển thị danh sách đơn hàng cho admin: " + e.getMessage(), e);
        }
    }

    @Transactional
    public OrderDetailResponse adminUpdateOrderStatus(Long orderId, String newStatus, String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus().equals("Cancelled")) {
            throw new RuntimeException("Cannot update status of a cancelled order");
        }
        order.setStatus(newStatus);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order).status(newStatus).updatedAt(LocalDateTime.now()).build();
        if (order.getStatusHistories() != null) order.getStatusHistories().add(history);
        else order.setStatusHistories(new java.util.LinkedHashSet<>(List.of(history)));

        orderRepository.save(order);
        return getOrderDetail(order.getCustomer().getCustomerId(), orderId);
    }
}
