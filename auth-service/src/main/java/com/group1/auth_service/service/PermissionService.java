package com.group1.auth_service.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.group1.auth_service.dto.request.PermissionRequest;
import com.group1.auth_service.dto.response.PermissionResponse;
import com.group1.auth_service.entity.Permission;
import com.group1.auth_service.repository.PermissionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    // CREATE PERMISSION
    @Transactional
    public PermissionResponse create(PermissionRequest request) {

        if (isBlank(request.getName())
                || isBlank(request.getResource())
                || isBlank(request.getAction())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "name, resource and action are required");
        }

        // Chuẩn hóa dữ liệu về UPPERCASE để đồng bộ hệ thống
        String name = request.getName().trim().toUpperCase();
        String resource = request.getResource().trim().toUpperCase();
        String action = request.getAction().trim().toUpperCase();

        if (permissionRepository.existsByName(name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Permission name already exists");
        }

        if (permissionRepository.existsByResourceAndAction(resource, action)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Permission already exists");
        }

        Permission permission = new Permission();
        permission.setName(name);
        permission.setResource(resource);
        permission.setAction(action);
        permission.setDescription(request.getDescription());

        Permission saved = permissionRepository.save(permission);
        return mapToResponse(saved);
    }

    // GET PERMISSIONS (pagination + filter)
    public Page<PermissionResponse> getPermissions(
            String resource,
            String action,
            Pageable pageable) {

        String normalizedResource = resource != null ? resource.trim().toUpperCase() : null;
        String normalizedAction = action != null ? action.trim().toUpperCase() : null;

        Page<Permission> page;

        if (normalizedResource != null && normalizedAction != null) {
            page = permissionRepository.findByResourceAndAction(
                    normalizedResource, normalizedAction, pageable);

        } else if (normalizedResource != null) {
            page = permissionRepository.findByResource(
                    normalizedResource, pageable);

        } else if (normalizedAction != null) {
            page = permissionRepository.findByAction(
                    normalizedAction, pageable);

        } else {
            page = permissionRepository.findAll(pageable);
        }

        return page.map(this::mapToResponse);
    }

    // GROUP BY RESOURCE (Giữ lại chức năng cũ của bạn)
    public Map<String, List<PermissionResponse>> groupByResource() {
        return permissionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.groupingBy(PermissionResponse::getResource));
    }

    // GET BY ID
    public PermissionResponse findById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Permission not found"));

        return mapToResponse(permission);
    }

    // UPDATE PERMISSION 
    // Giữ logic bảo vệ resource/action nhưng cho phép update description
    @Transactional
    public PermissionResponse update(Long id, PermissionRequest request) {

        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Permission not found"));

        // Kiểm tra nếu người dùng cố tình đổi Resource hoặc Action (gây lỗi Role)
        if (request.getResource() != null && !request.getResource().equalsIgnoreCase(permission.getResource())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot change resource of an existing permission");
        }
        
        if (request.getAction() != null && !request.getAction().equalsIgnoreCase(permission.getAction())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot change action of an existing permission");
        }

        // Cho phép cập nhật Description
        if (request.getDescription() != null) {
            permission.setDescription(request.getDescription());
        }
        
        // Cho phép cập nhật Name nếu không trùng (với điều kiện bạn cần)
        if (!isBlank(request.getName())) {
            String newName = request.getName().trim().toUpperCase();
            if (!newName.equals(permission.getName()) && permissionRepository.existsByName(newName)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "New permission name already exists");
            }
            permission.setName(newName);
        }

        Permission updated = permissionRepository.save(permission);
        return mapToResponse(updated);
    }

    // DELETE PERMISSION
    @Transactional
    public void delete(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Permission not found"));

        permissionRepository.delete(permission);
    }

    // MAPPER
    private PermissionResponse mapToResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .resource(permission.getResource())
                .action(permission.getAction())
                .description(permission.getDescription())
                .build();
    }

    // UTIL
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}