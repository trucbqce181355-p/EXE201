package com.group1.auth_service.service;

import com.group1.auth_service.dto.request.ForgotPasswordRequest;
import com.group1.auth_service.dto.request.ResetPasswordRequest;
import com.group1.auth_service.dto.request.UpdateAccountRequest;
import com.group1.auth_service.dto.request.UpdateAccountMultipartRequest;
import com.group1.auth_service.dto.response.AccountResponse;
import com.group1.auth_service.entity.Permission;
import com.group1.auth_service.entity.PasswordResetRequest;
import com.group1.auth_service.entity.PasswordResetToken;
import com.group1.auth_service.entity.Role;
import com.group1.auth_service.entity.User;
import com.group1.auth_service.entity.UserStatus;
import com.group1.auth_service.repository.PasswordResetRequestRepository;
import com.group1.auth_service.repository.PasswordResetTokenRepository;
import com.group1.auth_service.repository.UserRepository;
import com.group1.auth_service.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordResetRequestRepository passwordResetRequestRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AvatarStorageService avatarStorageService;

    @InjectMocks
    private AccountService accountService;

    @Test
    void updateAccountTrimsAndPersistsProfileFields() {
        User user = buildActiveUser();
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setFullName("  Story User  ");
        request.setPhone("  +84901234567  ");
        request.setAvatarUrl("  https://example.com/avatar.jpg  ");
        request.setAddress("  123 Story Street  ");
        request.setDateOfBirth(LocalDate.of(2000, 3, 20));

        when(userRepository.findByUsername("story-user")).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponse response = accountService.updateAccount("story-user", request);

        assertThat(user.getFullName()).isEqualTo("Story User");
        assertThat(user.getPhone()).isEqualTo("+84901234567");
        assertThat(user.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");
        assertThat(user.getAddress()).isEqualTo("123 Story Street");
        assertThat(user.getDateOfBirth()).isEqualTo(LocalDate.of(2000, 3, 20));
        assertThat(response.getFullName()).isEqualTo("Story User");
        assertThat(response.getPhone()).isEqualTo("+84901234567");
        assertThat(response.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");
        assertThat(response.getAddress()).isEqualTo("123 Story Street");
        assertThat(response.getDateOfBirth()).isEqualTo(LocalDate.of(2000, 3, 20));
        verify(userRepository).saveAndFlush(user);
    }

    @Test
    void updateAccountRejectsAddressLongerThan255Characters() {
        User user = buildActiveUser();
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setFullName("Story User");
        request.setPhone("+84901234567");
        request.setAvatarUrl("https://example.com/avatar.jpg");
        request.setAddress("x".repeat(256));

        when(userRepository.findByUsername("story-user")).thenReturn(Optional.of(user));

        ResponseStatusException exception = catchThrowableOfType(
                () -> accountService.updateAccount("story-user", request),
                ResponseStatusException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusCode().value()).isEqualTo(400);
        assertThat(exception.getReason()).isEqualTo("Address too long");
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void updateAccountRejectsFutureDateOfBirth() {
        User user = buildActiveUser();
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setFullName("Story User");
        request.setPhone("+84901234567");
        request.setAvatarUrl("https://example.com/avatar.jpg");
        request.setDateOfBirth(LocalDate.now().plusDays(1));

        when(userRepository.findByUsername("story-user")).thenReturn(Optional.of(user));

        ResponseStatusException exception = catchThrowableOfType(
                () -> accountService.updateAccount("story-user", request),
                ResponseStatusException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusCode().value()).isEqualTo(400);
        assertThat(exception.getReason()).isEqualTo("Date of birth cannot be in the future");
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void updateAccountStoresUploadedAvatarAndReturnsStoredUrl() {
        User user = buildActiveUser();
        MockMultipartFile avatarFile = new MockMultipartFile(
                "avatarFile",
                "avatar.png",
                "image/png",
                "avatar".getBytes()
        );
        UpdateAccountMultipartRequest request = new UpdateAccountMultipartRequest();
        request.setFullName("Story User");
        request.setPhone("+84901234567");
        request.setAddress("123 Story Street");
        request.setDateOfBirth(LocalDate.of(2000, 3, 20));
        request.setAvatarFile(avatarFile);

        when(userRepository.findByUsername("story-user")).thenReturn(Optional.of(user));
        when(avatarStorageService.storeAvatar(avatarFile)).thenReturn("http://localhost:8081/uploads/avatars/avatar.png");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponse response = accountService.updateAccount("story-user", request);

        assertThat(user.getAvatarUrl()).isEqualTo("http://localhost:8081/uploads/avatars/avatar.png");
        assertThat(response.getAvatarUrl()).isEqualTo("http://localhost:8081/uploads/avatars/avatar.png");
        verify(avatarStorageService).storeAvatar(avatarFile);
        verify(userRepository).saveAndFlush(user);
    }

    @Test
    void getAccountByIdAllowsReaderPermission() {
        User requester = buildActiveUser();
        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName("ADMIN");
        adminRole.setPermissions(new HashSet<>());
        Permission permission = new Permission();
        permission.setId(1L);
        permission.setName("CUSTOMER:READ");
        adminRole.getPermissions().add(permission);
        requester.getRoles().add(adminRole);

        User requestedUser = buildActiveUser();
        requestedUser.setId(99L);
        requestedUser.setUsername("customer-user");
        requestedUser.setEmail("customer@example.com");

        when(userRepository.findByUsername("story-user")).thenReturn(Optional.of(requester));
        when(userRepository.findById(99L)).thenReturn(Optional.of(requestedUser));

        AccountResponse response = accountService.getAccountById("story-user", 99L);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getUsername()).isEqualTo("customer-user");
    }

    @Test
    void getAccountByIdRejectsDifferentUserWithoutPermission() {
        User requester = buildActiveUser();

        when(userRepository.findByUsername("story-user")).thenReturn(Optional.of(requester));

        ResponseStatusException exception = catchThrowableOfType(
                () -> accountService.getAccountById("story-user", 99L),
                ResponseStatusException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusCode().value()).isEqualTo(403);
        assertThat(exception.getReason()).isEqualTo("Forbidden");
        verify(userRepository, never()).findById(99L);
    }

    @Test
    void requestForgotPasswordCreatesTokenAndSendsEmailForExistingUser() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("  Story.User@Example.com  ");

        User user = buildActiveUser();
        user.setEmail("story.user@example.com");

        when(passwordResetRequestRepository.countByEmailAndRequestedAtAfter(eq("story.user@example.com"), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(userRepository.findByEmail("story.user@example.com")).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        accountService.requestForgotPassword(request);

        ArgumentCaptor<PasswordResetRequest> requestCaptor = ArgumentCaptor.forClass(PasswordResetRequest.class);
        verify(passwordResetRequestRepository).save(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getEmail()).isEqualTo("story.user@example.com");

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getUser()).isSameAs(user);
        assertThat(savedToken.isUsed()).isFalse();
        assertThat(savedToken.getTokenHash()).hasSize(64);
        assertThat(savedToken.getExpiresAt()).isAfter(LocalDateTime.now().plusMinutes(14));

        ArgumentCaptor<String> rawTokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendPasswordResetToken(eq("story.user@example.com"), rawTokenCaptor.capture(), any(LocalDateTime.class));
        assertThat(rawTokenCaptor.getValue()).isNotBlank();
    }

    @Test
    void requestForgotPasswordKeepsGenericBehaviorForUnknownEmail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("missing@example.com");

        when(passwordResetRequestRepository.countByEmailAndRequestedAtAfter(eq("missing@example.com"), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        accountService.requestForgotPassword(request);

        verify(passwordResetRequestRepository).save(any(PasswordResetRequest.class));
        verify(passwordResetTokenRepository, never()).save(any(PasswordResetToken.class));
        verifyNoInteractions(emailService);
    }

    @Test
    void resetPasswordUpdatesPasswordMarksTokensUsedAndRevokesSessions() {
        User user = buildActiveUser();
        user.setPassword("encoded-old-password");

        PasswordResetToken token = new PasswordResetToken();
        token.setId(1L);
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        token.setUsed(false);

        PasswordResetToken secondToken = new PasswordResetToken();
        secondToken.setId(2L);
        secondToken.setUser(user);
        secondToken.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        secondToken.setUsed(false);

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("raw-reset-token");
        request.setNewPassword("New@1234");
        request.setConfirmPassword("New@1234");

        when(passwordResetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));
        when(passwordEncoder.matches("New@1234", "encoded-old-password")).thenReturn(false);
        when(passwordEncoder.encode("New@1234")).thenReturn("encoded-new-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordResetTokenRepository.findAllByUser_IdAndUsedFalse(user.getId()))
                .thenReturn(new ArrayList<>(List.of(token, secondToken)));

        accountService.resetPassword(request);

        assertThat(user.getPassword()).isEqualTo("encoded-new-password");
        assertThat(user.getPasswordChangedAt()).isNotNull();
        assertThat(token.isUsed()).isTrue();
        assertThat(token.getUsedAt()).isNotNull();
        assertThat(secondToken.isUsed()).isTrue();
        assertThat(secondToken.getUsedAt()).isNotNull();

        verify(refreshTokenService).revokeByUser(user);
        verify(passwordResetTokenRepository).save(token);
        verify(passwordResetTokenRepository).saveAll(org.mockito.ArgumentMatchers.<List<PasswordResetToken>>any());
    }

    private User buildActiveUser() {
        User user = new User();
        user.setId(10L);
        user.setUsername("story-user");
        user.setEmail("story.user@example.com");
        user.setFullName("Story User");
        user.setPhone("+84900000001");
        user.setAvatarUrl("https://example.com/original.jpg");
        user.setAddress("Old address");
        user.setDateOfBirth(LocalDate.of(1998, 1, 15));
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(new LinkedHashSet<>());
        user.setUpdatedAt(LocalDateTime.now());

        Role role = new Role();
        role.setId(1L);
        role.setName("USER");
        role.setPermissions(new HashSet<>());
        user.getRoles().add(role);
        return user;
    }
}
