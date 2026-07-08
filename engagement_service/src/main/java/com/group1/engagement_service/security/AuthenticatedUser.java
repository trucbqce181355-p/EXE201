package com.group1.engagement_service.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.security.Principal;

@Getter
@AllArgsConstructor
public class AuthenticatedUser implements Principal {
    private final Long userId;
    private final String username;
    private final String email;
    private final String fullName;

    @Override
    public String getName() {
        return username;
    }
}
