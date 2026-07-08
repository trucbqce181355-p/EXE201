package com.group1.auth_service.dto;

import java.util.Set;

import com.group1.auth_service.entity.UserStatus;

public class UserDTO {

    private Long id;
    private String username;
    private String phone;
    private String email;
    private String fullName;
    private UserStatus status;
    private Set<RoleDTO> roles;

    public UserDTO() {
    }

    public UserDTO(Long id, String username, String phone, String email, String fullName, UserStatus status, Set<RoleDTO> roles) {
        this.id = id;
        this.username = username;
        this.phone = phone;
        this.email = email;
        this.fullName = fullName;
        this.status = status;
        this.roles = roles;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Set<RoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleDTO> roles) {
        this.roles = roles;
    }

    public static class RoleDTO {
        private Long id;
        private String name;

        public RoleDTO(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
