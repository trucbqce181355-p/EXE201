package com.group1.auth_service.controller;

import com.group1.auth_service.dto.request.ChangePasswordRequest;
import com.group1.auth_service.dto.request.LoginRequest;
import com.group1.auth_service.dto.request.RefreshTokenRequest;
import com.group1.auth_service.dto.request.RegisterEmailOtpRequest;
import com.group1.auth_service.dto.request.RegisterRequest;
import com.group1.auth_service.dto.request.VerifyRegisterEmailOtpRequest;
import com.group1.auth_service.dto.response.ApiResponse;
import com.group1.auth_service.dto.response.AuthTokenResponse;
import com.group1.auth_service.service.AuthService;
import com.group1.auth_service.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import com.group1.auth_service.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;

import java.util.Map;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping({"/auth", "/api/auth"})
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, TokenBlacklistService tokenBlacklistService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register/request-otp")
    public ResponseEntity<?> requestRegisterOtp(@Valid @RequestBody RegisterEmailOtpRequest request) {
        try {
            authService.requestRegisterEmailOtp(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "OTP đã được gửi đến email", null));
        } catch (ResponseStatusException e) {
            String message = e.getReason();
            if (message == null || message.isBlank()) {
                message = "Không thể gửi OTP";
            }
            return ResponseEntity.status(e.getStatusCode().value())
                    .body(Map.of("error", message));
        }
    }

    @PostMapping("/register/verify-otp")
    public ResponseEntity<?> verifyRegisterOtp(@Valid @RequestBody VerifyRegisterEmailOtpRequest request) {
        try {
            authService.verifyRegisterEmailOtp(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Xác thực email thành công", null));
        } catch (ResponseStatusException e) {
            String message = e.getReason();
            if (message == null || message.isBlank()) {
                message = "Xác thực OTP thất bại";
            }
            return ResponseEntity.status(e.getStatusCode().value())
                    .body(Map.of("error", message));
        }
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthTokenResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResponseStatusException e) {
            String message = e.getReason();
            if (message == null || message.isBlank()) {
                message = "Đăng ký thất bại";
            }
            return ResponseEntity.status(e.getStatusCode().value())
                    .body(Map.of("error", message));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            System.out.println("vào login");
            System.out.println("email: " + request.getEmail());
            System.out.println("password: " + request.getPassword());
            AuthTokenResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid email or password"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "refreshToken is required"));
        }
        try {
            AuthTokenResponse response = authService.refresh(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                Date expiration = jwtUtil.extractExpiration(token);

                LocalDateTime expiryDate = expiration.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                tokenBlacklistService.blacklist(token, expiryDate);

            } catch (io.jsonwebtoken.security.SignatureException e) {
                return ResponseEntity.badRequest().body("The token is invalid or the signature is incorrect!");
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("There's an error with your token!");
            }
        }

        return ResponseEntity.ok("Logout success");
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {

        String header = httpRequest.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String token = header.substring(7);
        String username = jwtUtil.extractUsername(token);

        authService.changePassword(username, request);

        tokenBlacklistService.blacklist(token,
                LocalDateTime.now().plusHours(1));

        return ResponseEntity.ok("Password changed. Please login again.");
    }

}
