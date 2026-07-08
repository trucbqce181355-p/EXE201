package com.group1.auth_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleListItemResponse {

    private Long id;
    private String name;
    private String description;
    private Boolean isActive;

    // Number of users assigned to this role
    private long staffCount;

    // Permissions assigned to this role (full info, includes id & name)
    private Set<PermissionResponse> permissions = new LinkedHashSet<>();
}

