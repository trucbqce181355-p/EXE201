package com.group1.auth_service.dto.request;

import lombok.Data;
import java.util.Set;

@Data
public class UpdateUserRequest {
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String password;

    // Field mới để update role
    private Set<Long> roleIds;
}