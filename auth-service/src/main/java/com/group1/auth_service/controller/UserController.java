package com.group1.auth_service.controller;

import com.group1.auth_service.dto.UserDTO;
import com.group1.auth_service.dto.request.CreateUserRequest;
import com.group1.auth_service.dto.request.UpdateUserRequest;
import com.group1.auth_service.dto.response.UserBasicInfoResponse;
import com.group1.auth_service.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ResponseEntity<UserDTO> create(
            @RequestBody CreateUserRequest request,
            HttpServletRequest httpRequest) {
        System.out.println("Received request to create user with username: " + request.getUsername());
        String token = httpRequest.getHeader("Authorization");
        return ResponseEntity.status(201)
                .body(userService.createUser(request, token));
    }

    @PutMapping("/{id}/lock")
    @PreAuthorize("hasAuthority('USER_LOCK')")
    public ResponseEntity<UserDTO> lockUser(@PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(userService.lockUser(id, authentication.getName()));
    }

    @PutMapping("/{id}/unlock")
    @PreAuthorize("hasAuthority('USER_LOCK')")
    public ResponseEntity<UserDTO> unlockUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.unlockUser(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/batch/basic")
    public ResponseEntity<List<UserBasicInfoResponse>> getUsersBasic(@RequestBody List<Long> userIds) {
        return ResponseEntity.ok(userService.getUsersBasicByIds(userIds));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {

        String token = request.getHeader("Authorization");

        userService.deleteUser(id, token);

        return ResponseEntity.ok("User deleted successfully");
    }

    @PostMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<?> assignRoleToUser(@PathVariable Long userId,
            @PathVariable Long roleId,
            HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization");
        userService.assignRoleToUser(userId, roleId, token);
        return ResponseEntity.ok("Role assigned successfully");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id,
            @RequestBody UpdateUserRequest request,
            HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization");
        return ResponseEntity.ok(userService.updateUser(id, request, token));
    }

    
}
