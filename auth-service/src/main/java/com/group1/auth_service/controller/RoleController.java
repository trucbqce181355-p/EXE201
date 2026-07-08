package com.group1.auth_service.controller;

import com.group1.auth_service.dto.request.AssignPermissionRequest;
import com.group1.auth_service.dto.request.RoleRequest;
import com.group1.auth_service.dto.response.PermissionResponse;
import com.group1.auth_service.dto.response.RoleListItemResponse;
import com.group1.auth_service.entity.Permission;
import com.group1.auth_service.entity.Role;
import com.group1.auth_service.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    // ==============================
    // CREATE ROLE
    // ==============================
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE:CREATE')")
    public ResponseEntity<?> createRole(@Valid @RequestBody RoleRequest request) {

        Role savedRole = roleService.createRole(request);

        Map<String, Object> response = new HashMap<>();
        response.put("role_id", savedRole.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==============================
    // GET LIST OF ROLES (with pagination, search)
    // ==============================
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE:READ')")
    public ResponseEntity<?> getAllRoles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean is_active) {

        Page<Role> rolePage = roleService.getAllRoles(page, limit, search, is_active);

        List<RoleListItemResponse> items = rolePage.getContent().stream()
                .map(role -> {
                    long staffCount = roleService.countUsersByRoleId(role.getId());
                    Set<PermissionResponse> permissionDtos = role.getPermissions() == null
                            ? new LinkedHashSet<>()
                            : role.getPermissions().stream()
                                .map(permission -> PermissionResponse.builder()
                                        .id(permission.getId())
                                        .name(permission.getName())
                                        .resource(permission.getResource())
                                        .action(permission.getAction())
                                        .description(permission.getDescription())
                                        .build())
                                .collect(Collectors.toCollection(LinkedHashSet::new));

                    return new RoleListItemResponse(
                            role.getId(),
                            role.getName(),
                            role.getDescription(),
                            role.getIsActive(),
                            staffCount,
                            permissionDtos
                    );
                })
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("data", items);
        response.put("total", rolePage.getTotalElements());
        response.put("page", page);
        response.put("limit", limit);

        return ResponseEntity.ok(response);
    }

    // ==============================
    // GET ROLE BY ID
    // ==============================
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE:READ')")
    public ResponseEntity<?> getRoleById(@PathVariable Long id) {

        Role role = roleService.getRoleById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("data", role);
        // Role entity already includes permissions via @ManyToMany

        return ResponseEntity.ok(response);
    }

    // ==============================
    // UPDATE ROLE
    // ==============================
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE:UPDATE')")
    public ResponseEntity<?> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request) {

        Role updatedRole = roleService.updateRole(id, request);

        Map<String, Object> response = new HashMap<>();
        response.put("data", updatedRole);

        return ResponseEntity.ok(response);
    }

    // ==============================
    // DELETE ROLE
    // ==============================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE:DELETE')")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {

        roleService.deleteRole(id);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Role deleted");

        return ResponseEntity.ok(response);
    }

    // ==============================
    // Assign Permissions to Role
    // ==============================
    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE:UPDATE')")
    public ResponseEntity<?> assignPermissions(
            @PathVariable Long id,
            @Valid @RequestBody AssignPermissionRequest request) {

        roleService.assignPermissions(id, request.getPermissionIds());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Permissions assigned successfully");
        response.put("roleId", id);

        return ResponseEntity.ok(response);
    }

    // ==============================
    // Remove Permissions
    // ==============================
    @DeleteMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE:UPDATE')")
    public ResponseEntity<?> removePermissions(
            @PathVariable Long id,
            @Valid @RequestBody AssignPermissionRequest request) {

        roleService.removePermissions(id, request.getPermissionIds());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Permissions removed successfully");
        response.put("roleId", id);

        return ResponseEntity.ok(response);
    }

    // ==============================
    // Get Role Permissions
    // ==============================
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE:READ')")
    public ResponseEntity<?> getPermissions(@PathVariable Long id) {

        Set<Permission> permissions = roleService.getRolePermissions(id);

        return ResponseEntity.ok(permissions);
    }
}