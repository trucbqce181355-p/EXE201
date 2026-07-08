package com.group1.auth_service.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PermissionResponse {

    private Long id;
    private String name;
    private String resource;
    private String action;
    private String description;

}