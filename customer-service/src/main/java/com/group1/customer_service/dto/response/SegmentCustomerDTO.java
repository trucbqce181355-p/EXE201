package com.group1.customer_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SegmentCustomerDTO {
    private Long id;
    private String name;
    private String email;
    private Double totalSpent;
    private Long orderCount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", timezone = "Asia/Ho_Chi_Minh")
    private LocalDate lastOrderDate;
    private String loyaltyTier;
    private String location;
}

