package com.group1.production_service.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TagRequest {

    @NotBlank(message = "Tag name is required")
    @Size(max = 100, message = "Tag name must be <= 100 characters")
    private String name;

    @NotBlank(message = "Tag slug is required")
    @Size(max = 120, message = "Tag slug must be <= 120 characters")
    private String slug;

    @Size(max = 255, message = "Description must be <= 255 characters")
    private String description;

    private Boolean active;
}
