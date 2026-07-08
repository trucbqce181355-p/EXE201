package com.group1.auth_service.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleResponse {

    private Long id;
    private String name;
    private String description;
   
}