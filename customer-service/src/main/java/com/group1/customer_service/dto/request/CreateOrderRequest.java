package com.group1.customer_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderRequest {

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Receiver name is required")
    private String receiverName;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private BigDecimal discountAmount;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<CreateOrderItemRequest> items;
}
