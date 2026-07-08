package com.group1.auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.group1.auth_service.dto.request.UpdateAccountMultipartRequest;
import com.group1.auth_service.dto.request.UpdateAccountRequest;
import com.group1.auth_service.dto.response.AccountResponse;
import com.group1.auth_service.exception.GlobalExceptionHandler;
import com.group1.auth_service.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void updateAccountReturnsUpdatedProfile() throws Exception {
        AccountResponse response = new AccountResponse(
                10L,
                "story-user",
                "story.user@example.com",
                "Story User",
                "+84901234567",
                "https://example.com/avatar.jpg",
                "123 Story Street",
                LocalDate.of(2000, 3, 20),
                "MALE",
                "ACTIVE",
                new LinkedHashSet<>(Set.of("USER")),
                LocalDateTime.of(2026, 3, 10, 8, 45),
                LocalDateTime.of(2026, 3, 1, 9, 0),
                LocalDateTime.of(2026, 3, 11, 10, 30)
        );

        when(accountService.updateAccount(eq("story-user"), any(UpdateAccountRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/accounts/me")
                        .principal(new UsernamePasswordAuthenticationToken("story-user", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "Story User",
                                "phone", "+84901234567",
                                "avatarUrl", "https://example.com/avatar.jpg",
                                "address", "123 Story Street",
                                "dateOfBirth", "2000-03-20"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("story-user"))
                .andExpect(jsonPath("$.data.phone").value("+84901234567"))
                .andExpect(jsonPath("$.data.address").value("123 Story Street"))
                .andExpect(jsonPath("$.data.dateOfBirth").value("2000-03-20"));
    }

    @Test
    void getAccountByIdReturnsRequestedProfile() throws Exception {
        AccountResponse response = new AccountResponse(
                11L,
                "customer-user",
                "customer@example.com",
                "Customer User",
                "+84901234568",
                "https://example.com/customer.jpg",
                "234 Customer Lane",
                LocalDate.of(1999, 5, 10),
                "FEMALE",
                "ACTIVE",
                new LinkedHashSet<>(Set.of("ROLE_CUSTOMER")),
                null,
                LocalDateTime.of(2026, 2, 1, 9, 0),
                LocalDateTime.of(2026, 3, 1, 10, 0)
        );

        when(accountService.getAccountById("admin-user", 11L)).thenReturn(response);

        mockMvc.perform(get("/api/accounts/11")
                        .principal(new UsernamePasswordAuthenticationToken("admin-user", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(11))
                .andExpect(jsonPath("$.data.email").value("customer@example.com"));
    }

    @Test
    void updateAccountMultipartAcceptsAvatarUpload() throws Exception {
        AccountResponse response = new AccountResponse(
                10L,
                "story-user",
                "story.user@example.com",
                "Story User",
                "+84901234567",
                "http://localhost:8081/uploads/avatars/avatar.png",
                "123 Story Street",
                LocalDate.of(2000, 3, 20),
                "MALE",
                "ACTIVE",
                new LinkedHashSet<>(Set.of("USER")),
                LocalDateTime.of(2026, 3, 10, 8, 45),
                LocalDateTime.of(2026, 3, 1, 9, 0),
                LocalDateTime.of(2026, 3, 11, 10, 30)
        );
        MockMultipartFile avatarFile = new MockMultipartFile(
                "avatarFile",
                "avatar.png",
                "image/png",
                "avatar".getBytes()
        );

        when(accountService.updateAccount(eq("story-user"), any(UpdateAccountMultipartRequest.class))).thenReturn(response);

        mockMvc.perform(multipart("/api/accounts/me")
                        .file(avatarFile)
                        .param("fullName", "Story User")
                        .param("phone", "+84901234567")
                        .param("address", "123 Story Street")
                        .param("dateOfBirth", "2000-03-20")
                        .principal(new UsernamePasswordAuthenticationToken("story-user", null))
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.avatarUrl").value("http://localhost:8081/uploads/avatars/avatar.png"));
    }

    @Test
    void updateAccountRejectsInvalidPhonePayload() throws Exception {
        mockMvc.perform(put("/api/accounts/me")
                        .principal(new UsernamePasswordAuthenticationToken("story-user", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "Story User",
                                "phone", "abc",
                                "avatarUrl", "https://example.com/avatar.jpg"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid phone format"));

        verifyNoInteractions(accountService);
    }

    @Test
    void updateAccountRejectsInvalidDatePayload() throws Exception {
        mockMvc.perform(put("/api/accounts/me")
                        .principal(new UsernamePasswordAuthenticationToken("story-user", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Story User",
                                  "phone": "+84901234567",
                                  "avatarUrl": "https://example.com/avatar.jpg",
                                  "dateOfBirth": "20-03-2000"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid date format for dateOfBirth. Use yyyy-MM-dd"));

        verifyNoInteractions(accountService);
    }

    @Test
    void forgotPasswordRejectsInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/accounts/forgot-password/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "abc"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email format"));

        verifyNoInteractions(accountService);
    }

    @Test
    void resetPasswordReturnsSuccessEnvelope() throws Exception {
        doNothing().when(accountService).resetPassword(any());

        mockMvc.perform(post("/api/accounts/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", "reset-token",
                                "newPassword", "New@1234",
                                "confirmPassword", "New@1234"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset successfully"));
    }
}
