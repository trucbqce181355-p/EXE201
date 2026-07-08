package com.group1.auth_service.service;

import com.group1.auth_service.entity.RefreshToken;
import com.group1.auth_service.entity.User;
import com.group1.auth_service.repository.UserRepository;
import com.group1.auth_service.security.CustomUserDetailsService;
import com.group1.auth_service.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthService authService;

    @Test
    void refreshRejectsTokensIssuedBeforePasswordChange() {
        String refreshTokenValue = "refresh-token";
        LocalDateTime passwordChangedAt = LocalDateTime.now();

        User user = new User();
        user.setUsername("story-user");
        user.setPasswordChangedAt(passwordChangedAt);

        RefreshToken storedToken = new RefreshToken();
        storedToken.setToken(refreshTokenValue);
        storedToken.setUser(user);
        storedToken.setExpiresAt(LocalDateTime.now().plusDays(1));

        Date issuedAt = Date.from(passwordChangedAt.minusMinutes(5).atZone(ZoneId.systemDefault()).toInstant());
        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.withUsername("story-user")
                        .password("encoded")
                        .authorities("ROLE_USER")
                        .build();

        when(jwtUtil.isRefreshTokenValid(refreshTokenValue)).thenReturn(true);
        when(refreshTokenService.findByToken(refreshTokenValue)).thenReturn(storedToken);
        when(jwtUtil.extractUsername(refreshTokenValue)).thenReturn("story-user");
        when(userDetailsService.loadUserByUsername("story-user")).thenReturn(userDetails);
        when(userRepository.findByUsername("story-user")).thenReturn(Optional.of(user));
        when(jwtUtil.extractIssuedAt(refreshTokenValue)).thenReturn(issuedAt);

        assertThatThrownBy(() -> authService.refresh(refreshTokenValue))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid or expired refresh token");

        verify(refreshTokenService).revokeByToken(refreshTokenValue);
        verify(refreshTokenService, never()).createAndSaveRefreshToken(any(User.class), anyString());
    }
}
