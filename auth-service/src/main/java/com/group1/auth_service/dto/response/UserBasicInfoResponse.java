package com.group1.auth_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserBasicInfoResponse {
    private Long id;
    private String fullName;
    private String email;
}
