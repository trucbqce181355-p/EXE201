package com.group1.customer_service.client;

import com.group1.customer_service.dto.request.UpdateProfileRequest;
import com.group1.customer_service.dto.response.ApiResponse;
import com.group1.customer_service.dto.response.AuthAccountResponse;
import com.group1.customer_service.dto.response.AuthUserBasicDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "auth-service", url = "http://localhost:8081")
public interface AuthClient {

    // Lấy thông tin tài khoản chi tiết theo ID
    @GetMapping("/api/accounts/{id}")
    ApiResponse<AuthAccountResponse> getAccountById(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") Long id);

    // Cập nhật thông tin cá nhân
    @PutMapping("/api/accounts/me")
    void updateAuthProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody UpdateProfileRequest request);

    // Lấy danh sách thông tin cơ bản của người dùng theo lô (Batch)
    @PostMapping("/users/batch/basic")
    List<AuthUserBasicDTO> getUsersBasic(
            @RequestHeader("Authorization") String token,
            @RequestBody List<Long> userIds);
}