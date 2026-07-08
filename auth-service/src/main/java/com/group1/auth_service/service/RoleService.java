package com.group1.auth_service.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.group1.auth_service.dto.request.RoleRequest;
import com.group1.auth_service.entity.Permission;
import com.group1.auth_service.entity.Role;
import com.group1.auth_service.repository.PermissionRepository;
import com.group1.auth_service.repository.RoleRepository;
import com.group1.auth_service.repository.UserRepository;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    private static final Set<String> SYSTEM_ROLES = Set.of("SUPER_ADMIN");

    public RoleService(RoleRepository roleRepository,
                       PermissionRepository permissionRepository,
                       UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Role getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        role.getPermissions().size(); // Ép load Permissions
        return role;
    }

    @Transactional
    public Role createRole(RoleRequest request) {
        String trimmedName = request.getName() == null ? null : request.getName().trim().toUpperCase();

        if (!StringUtils.hasText(trimmedName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role name is required");
        }

        if (roleRepository.findByName(trimmedName).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Role name already exists");
        }

        Role role = new Role();
        role.setName(trimmedName);
        role.setDescription(request.getDescription());
        role.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            List<Permission> foundPermissions = permissionRepository.findAllById(request.getPermissionIds());
            if (foundPermissions.size() != request.getPermissionIds().size()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Some permissions not found");
            }
            role.setPermissions(new HashSet<>(foundPermissions));
        }
        return roleRepository.save(role);
    }

    @Transactional(readOnly = true)
    public Page<Role> getAllRoles(int page, int limit, String search, Boolean isActive) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("id").ascending());
        Page<Role> roles;

        if (StringUtils.hasText(search) && isActive != null) {
            roles = roleRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndIsActive(search, search, isActive, pageable);
        } else if (StringUtils.hasText(search)) {
            roles = roleRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search, pageable);
        } else if (isActive != null) {
            roles = roleRepository.findByIsActive(isActive, pageable);
        } else {
            roles = roleRepository.findAll(pageable);
        }
        
        roles.forEach(r -> r.getPermissions().size()); // Quan trọng để hiển thị Permission
        return roles;
    }

    @Transactional
    public Role updateRole(Long id, RoleRequest request) {
        Role existingRole = getRoleById(id);

        if (SYSTEM_ROLES.contains(existingRole.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot update system role");
        }

        String nextName = request.getName() == null ? null : request.getName().trim().toUpperCase();
        if (StringUtils.hasText(nextName)) {
            if (!existingRole.getName().equals(nextName) && roleRepository.findByName(nextName).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Role name already exists");
            }
            existingRole.setName(nextName);
        }

        existingRole.setDescription(request.getDescription());
        if (request.getIsActive() != null) existingRole.setIsActive(request.getIsActive());

        if (request.getPermissionIds() != null) {
            if (!request.getPermissionIds().isEmpty()) {
                List<Permission> foundPermissions = permissionRepository.findAllById(request.getPermissionIds());
                if (foundPermissions.size() != request.getPermissionIds().size()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Permissions not found");
                }
                existingRole.setPermissions(new HashSet<>(foundPermissions));
            } else {
                existingRole.getPermissions().clear();
            }
        }
        return roleRepository.save(existingRole);
    }

    @Transactional
    public void deleteRole(Long id) {
        Role role = getRoleById(id);
        if (SYSTEM_ROLES.contains(role.getName())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "System role");
        if (userRepository.existsByRoleId(id)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role has users");
        roleRepository.delete(role);
    }

    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        Role role = getRoleById(roleId);
        List<Permission> found = permissionRepository.findAllById(permissionIds);
        if (found.size() != permissionIds.size()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Permissions not found");
        role.getPermissions().addAll(found);
        roleRepository.save(role);
    }

    @Transactional
    public void removePermissions(Long roleId, List<Long> permissionIds) {
        Role role = getRoleById(roleId);
        role.getPermissions().removeIf(p -> permissionIds.contains(p.getId()));
        roleRepository.save(role);
    }

    @Transactional(readOnly = true)
    public Set<Permission> getRolePermissions(Long roleId) {
        return getRoleById(roleId).getPermissions();
    }

    public long countUsersByRoleId(Long roleId) { return userRepository.countByRoleId(roleId); }
}