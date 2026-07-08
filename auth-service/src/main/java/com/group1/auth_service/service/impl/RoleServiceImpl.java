package com.group1.auth_service.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.group1.auth_service.dto.request.RoleRequest;
import com.group1.auth_service.dto.response.RoleResponse;
import com.group1.auth_service.entity.Role;
import com.group1.auth_service.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl {

    private final RoleRepository roleRepository;

    public RoleResponse createRole(RoleRequest request) {

        if (roleRepository.existsByName(request.getName())) {
            throw new RuntimeException("Role already exists");
        }

        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription()); // ✅ THÊM

        roleRepository.save(role);

        return mapToResponse(role);
    }


    public List<RoleResponse> getAllRoles() {

        return roleRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    public RoleResponse updateRole(Long id, RoleRequest request) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (!role.getName().equals(request.getName())
                && roleRepository.existsByName(request.getName())) {
            throw new RuntimeException("Role name already exists");
        }

        role.setName(request.getName());
        role.setDescription(request.getDescription()); // ✅ THÊM

        roleRepository.save(role);

        return mapToResponse(role);
    }

    public void deleteRole(Long id) {

        if (!roleRepository.existsById(id)) {
            throw new RuntimeException("Role not found");
        }

        roleRepository.deleteById(id);
    }

    // ================= MAPPER =================
    private RoleResponse mapToResponse(Role role) {

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription()) // ✅ THÊM
                .build();
    }
}