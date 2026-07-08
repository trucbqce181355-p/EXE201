package com.group1.customer_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSegmentRequest {

    @NotBlank(message = "Segment name is required")
    @Size(max = 150, message = "Segment name must be <= 150 characters")
    private String name;

    @Size(max = 500, message = "Description must be <= 500 characters")
    private String description;

    @NotNull(message = "Invalid criteria format")
    @Valid
    private SegmentCriteriaDTO criteria;
}

