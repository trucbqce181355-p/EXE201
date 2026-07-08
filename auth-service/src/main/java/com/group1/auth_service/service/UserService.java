package com.group1.auth_service.service;

import com.group1.auth_service.dto.UserDTO;
import com.group1.auth_service.dto.request.CreateUserRequest;
import com.group1.auth_service.dto.request.RegisterRequest;
import com.group1.auth_service.dto.request.UpdateUserRequest;
import com.group1.auth_service.dto.response.UserBasicInfoResponse;
import com.group1.auth_service.entity.Role;
import com.group1.auth_service.entity.User;
import com.group1.auth_service.entity.UserStatus;
import com.group1.auth_service.repository.RoleRepository;
import com.group1.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpMethod;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final RestTemplate restTemplate;

    private static final String CUSTOMER_ROLE = "ROLE_CUSTOMER";
    private static final String CUSTOMER_SERVICE_URL = "http://localhost:8082/customers";

    // =========================
    // CREATE USER
    // =========================
    @Transactional
    public UserDTO createUser(CreateUserRequest request, String token) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setStatus(UserStatus.ACTIVE);
        System.out.println("Creating user with username: " + user.getUsername());
        Set<Role> roles = request.getRoleIds().stream()
                .map(id -> roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Role " + id + " not found")))
                .collect(Collectors.toSet());
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        System.out.println("User created with ID: " + savedUser.getId());
        System.out.println("Before call customer service");

        System.out.println("After call customer service");
        System.out.println("User roles: " + roles.stream().map(Role::getName).collect(Collectors.joining(", ")));
        // Nếu có role CUSTOMER → gọi customer-service
        if (roles.stream().anyMatch(r -> CUSTOMER_ROLE.equals(r.getName()))) {
            System.out.println("User has CUSTOMER role, calling customer service...");
            try {
                callCustomerServiceCreate(savedUser.getId(), token);
            } catch (Exception e) {
                e.printStackTrace(); // in lỗi ra
            }
        }

        return mapToDTO(savedUser);
    }

    // =========================
    // REGISTER PUBLIC
    // =========================
    @Transactional
    public User registerPublic(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        Role defaultRole = roleRepository.findByName(CUSTOMER_ROLE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Default role CUSTOMER not found"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(new HashSet<>(Set.of(defaultRole)));

        User savedUser = userRepository.save(user);

        return savedUser;
    }

    public List<UserBasicInfoResponse> getUsersBasicByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return userRepository.findAllById(userIds).stream()
                .map(user -> new UserBasicInfoResponse(user.getId(), user.getFullName(), user.getEmail()))
                .collect(Collectors.toList());
    }

    // =========================
    // UPDATE USER
    // =========================
    @Transactional
    public UserDTO updateUser(Long id, UpdateUserRequest request, String token) {

        System.out.println("Updating user with ID: " + id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Lưu role cũ
        boolean wasCustomer = user.getRoles().stream()
                .anyMatch(r -> CUSTOMER_ROLE.equals(r.getName()));

        System.out.println("Current user roles: " + user.getRoles());

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {

            Set<Role> newRoles = request.getRoleIds().stream()
                    .map(rid -> roleRepository.findById(rid)
                    .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Role " + rid + " not found")))
                    .collect(Collectors.toSet());

            user.setRoles(newRoles);

            System.out.println("Updated roles: "
                    + newRoles.stream().map(Role::getName).collect(Collectors.joining(", ")));

            // Kiểm tra role mới
            boolean isCustomerNow = newRoles.stream()
                    .anyMatch(r -> CUSTOMER_ROLE.equals(r.getName()));

            // CHỈ gọi khi từ KHÔNG phải → THÀNH CUSTOMER
            if (!wasCustomer && isCustomerNow) {
                callCustomerServiceCreate(user.getId(), token);
            }
        }

        userRepository.save(user);
        return mapToDTO(user);
    }

    // =========================
    // ASSIGN ROLE TO USER
    // =========================
    @Transactional
    public void assignRoleToUser(Long userId, Long roleId, String token) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (user.getRoles().stream().anyMatch(r -> r.getId().equals(roleId))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already has this role");
        }

        user.getRoles().add(role);
        userRepository.save(user);

        if (CUSTOMER_ROLE.equals(role.getName())) {
            callCustomerServiceCreate(userId, token);
        }
    }

    // =========================
    // LOCK / UNLOCK USER
    // =========================
    @Transactional
    public UserDTO lockUser(Long id, String currentUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getUsername().equals(currentUsername)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot lock yourself");
        }
        if (user.getRoles().stream().anyMatch(r -> "SUPER_ADMIN".equals(r.getName()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot lock SUPER_ADMIN");
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already locked");
        }

        user.setStatus(UserStatus.LOCKED);
        userRepository.save(user);
        refreshTokenService.revokeAllByUser(user);

        return mapToDTO(user);
    }

    @Transactional
    public UserDTO unlockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getStatus() != UserStatus.LOCKED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not locked");
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        return mapToDTO(user);
    }

    // =========================
    // DELETE USER
    // =========================
    @Transactional
    public void deleteUser(Long id, String token) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // ❌ Không cho xóa SUPER_ADMIN
        if (user.getRoles().stream().anyMatch(r -> "SUPER_ADMIN".equals(r.getName()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete SUPER_ADMIN");
        }

        boolean isCustomer = user.getRoles().stream()
                .anyMatch(r -> "ROLE_CUSTOMER".equals(r.getName()));

        // 👉 Nếu là CUSTOMER → gọi customer-service trước
        if (isCustomer) {
            callCustomerServiceDelete(user.getId(), token);
        }

        // revoke token
        refreshTokenService.revokeAllByUser(user);

        // xóa user
        userRepository.deleteById(id);
    }

    // =========================
    // CALL CUSTOMER SERVICE
    // =========================
    private void callCustomerServiceCreate(Long userId, String token) {

        System.out.println("Calling customer service to create profile for user ID: " + userId);
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8082/customers";

        Map<String, Object> request = new HashMap<>();
        request.put("userId", userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // ⚠️ dùng token từ request
        headers.set("Authorization", token);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        restTemplate.postForEntity(url, entity, Void.class);
    }

    // =========================
    // GET ALL USERS
    // =========================
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // MAP TO DTO
    // =========================
    private UserDTO mapToDTO(User user) {
        Set<UserDTO.RoleDTO> roleDTOs = user.getRoles().stream()
                .map(role -> new UserDTO.RoleDTO(role.getId(), role.getName()))
                .collect(Collectors.toSet());

        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getPhone(),
                user.getEmail(),
                user.getFullName(),
                user.getStatus(),
                roleDTOs);
    }

    private void callCustomerServiceDelete(Long userId, String token) {
        String url = "http://localhost:8082/customers/user/" + userId;
        System.out.println("Vao callCustomerServiceDelete");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to delete customer profile");
        }
    }
}
