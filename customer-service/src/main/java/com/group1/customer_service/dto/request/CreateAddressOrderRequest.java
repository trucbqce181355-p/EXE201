package com.group1.customer_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAddressOrderRequest {
    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address too long")
    private String addressLine;
    private Boolean isDefault;
}
