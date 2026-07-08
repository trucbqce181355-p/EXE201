package com.group1.customer_service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthUserBasicDTO {
    private Long id;
    private String fullName;
    private String email;
}
