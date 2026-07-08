package com.group1.customer_service.dto.request;

import com.group1.customer_service.entity.CustomerSegment;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCustomerSegmentRequest {

    @NotNull(message = "Segment is required")
    private CustomerSegment segment;
}
