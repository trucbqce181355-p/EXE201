package com.group1.auth_service.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleRequest {

    @NotBlank(message = "Role name is required")
    private String name;

    // Mô tả role
    private String description;

    // Trạng thái active của role
    private Boolean isActive;

    // Danh sách permissionIds gán cho role (tùy chọn)
    private List<Long> permissionIds;
}