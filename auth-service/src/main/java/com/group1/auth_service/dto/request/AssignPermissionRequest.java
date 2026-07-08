package com.group1.auth_service.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class AssignPermissionRequest {

    @NotEmpty(message = "permissionIds must not be empty")
    private List<Long> permissionIds;

    public AssignPermissionRequest() {
    }

    public AssignPermissionRequest(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }

    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }
}