package com.group1.production_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AssignProductTagsRequest {

    @NotNull(message = "Tag ids are required")
    private List<Long> tagIds;
}
