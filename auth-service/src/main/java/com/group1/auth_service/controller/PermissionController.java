package com.group1.auth_service.controller;

import com.group1.auth_service.dto.request.PermissionRequest;
import com.group1.auth_service.dto.response.ApiResponse;
import com.group1.auth_service.dto.response.PermissionResponse;
import com.group1.auth_service.service.PermissionService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    // CREATE
    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION:CREATE')")
    public ApiResponse<PermissionResponse> createPermission(
            @RequestBody PermissionRequest request) {

        return new ApiResponse<>(
                true,
                "Permission created successfully",
                permissionService.create(request));
    }

    // GET LIST + FILTER + PAGINATION + GROUP BY
    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION:READ')")
    public ApiResponse<?> getPermissions(
            @RequestParam(required = false) String resource,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String groupBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Chức năng GroupBy của bạn
        if ("resource".equals(groupBy)) {
            return new ApiResponse<>(
                    true,
                    "Permissions grouped by resource fetched successfully",
                    permissionService.groupByResource());
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<PermissionResponse> permissions = permissionService.getPermissions(resource, action, pageable);

        return new ApiResponse<>(
                true,
                "Permissions fetched successfully",
                permissions);
    }

    // GET BY ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION:READ')")
    public ApiResponse<PermissionResponse> getPermissionById(
            @PathVariable Long id) {

        return new ApiResponse<>(
                true,
                "Permission fetched successfully",
                permissionService.findById(id));
    }

    // UPDATE
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION:UPDATE')")
    public ApiResponse<PermissionResponse> updatePermission(
            @PathVariable Long id,
            @RequestBody PermissionRequest request) {

        return new ApiResponse<>(
                true,
                "Permission updated successfully",
                permissionService.update(id, request));
    }

    // DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION:DELETE')")
    public ApiResponse<Void> deletePermission(
            @PathVariable Long id) {

        permissionService.delete(id);

        return new ApiResponse<>(
                true,
                "Permission deleted successfully",
                null);
    }
}