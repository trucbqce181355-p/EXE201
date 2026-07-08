package com.group1.auth_service.controller;

import com.group1.auth_service.dto.request.ForgotPasswordRequest;
import com.group1.auth_service.dto.request.ResetPasswordRequest;
import com.group1.auth_service.dto.request.UpdateAccountRequest;
import com.group1.auth_service.dto.request.UpdateAccountMultipartRequest;
import com.group1.auth_service.dto.response.AccountResponse;
import com.group1.auth_service.dto.response.ApiResponse;
import com.group1.auth_service.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/forgot-password/request")
    public ResponseEntity<ApiResponse<Void>> requestForgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        accountService.requestForgotPassword(request);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Reset link sent to email",
                null
        ));
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        accountService.resetPassword(request);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Password reset successfully",
                null
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication token");
        }

        AccountResponse account = accountService.getAccount(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Account loaded successfully.",
                account
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(
            Authentication authentication,
            @PathVariable("id") Long id) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication token");
        }

        AccountResponse account = accountService.getAccountById(authentication.getName(), id);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Account loaded successfully.",
                account
        ));
    }

    @PutMapping(path = "/me", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccount(
            Authentication authentication,
            @Valid @RequestBody UpdateAccountRequest request) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication token");
        }
        AccountResponse updated = accountService.updateAccount(authentication.getName(), request);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Account updated successfully.",
                updated
        ));
    }

    @PutMapping(path = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccountWithAvatarUpload(
            Authentication authentication,
            @Valid @ModelAttribute UpdateAccountMultipartRequest request) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication token");
        }
        AccountResponse updated = accountService.updateAccount(authentication.getName(), request);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Account updated successfully.",
                updated
        ));
    }
}
