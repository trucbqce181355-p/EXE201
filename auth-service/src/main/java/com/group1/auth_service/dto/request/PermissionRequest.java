package com.group1.auth_service.dto.request;

import lombok.Data;

@Data
public class PermissionRequest {

    private String name;
    private String resource;
    private String action;
    private String description;
}
