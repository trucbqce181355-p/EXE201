package com.group1.customer_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    @NotBlank(message = "New status is required")
    private String newStatus;
    
    private String note;
}
