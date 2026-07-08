package com.group1.auth_service.service;

import com.group1.auth_service.dto.request.AuthLoginRequest;
import com.group1.auth_service.dto.request.ForgotPasswordRequest;
import com.group1.auth_service.dto.request.ResetPasswordRequest;
import com.group1.auth_service.dto.request.UpdateAccountRequest;
import com.group1.auth_service.dto.request.UpdateAccountMultipartRequest;
import com.group1.auth_service.dto.response.AccountResponse;
import com.group1.auth_service.dto.response.AuthTokenResponse;
import com.group1.auth_service.entity.PasswordResetRequest;
import com.group1.auth_service.entity.PasswordResetToken;
import com.group1.auth_service.entity.User;
import com.group1.auth_service.entity.UserStatus;
import com.group1.auth_service.repository.PasswordResetRequestRepository;
import com.group1.auth_service.repository.PasswordResetTokenRepository;
import com.group1.auth_service.repository.UserRepository;
import com.group1.auth_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {
    private static final int RESET_TOKEN_TTL_MINUTES = 15;
    private static final int RESET_REQUEST_LIMIT_PER_HOUR = 3;
    private static final int ADDRESS_MAX_LENGTH = 255;
    private static final Pattern PASSWORD_POLICY =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9]{9,15}$");

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetRequestRepository passwordResetRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;
    private final AvatarStorageService avatarStorageService;
    private final JwtUtil jwtService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthTokenResponse login(AuthLoginRequest request) {
        String identifier = request.getIdentifier().trim();
        Optional<User> userOpt;
        if (identifier.contains("@")) {
            userOpt = userRepository.findByEmail(identifier.toLowerCase());
        } else {
            userOpt = userRepository.findByUsername(identifier);
        }

        User user = userOpt.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account is not active");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                        .password(user.getPassword())
                        .authorities("ROLE_USER")
                        .build();

        String accessToken = jwtService.generateToken(userDetails);
        return AuthTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(null)
                .build();
    }

    @Transactional
    public void requestForgotPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());
        LocalDateTime now = LocalDateTime.now();

        long recentRequestCount = passwordResetRequestRepository
                .countByEmailAndRequestedAtAfter(email, now.minusHours(1));
        if (recentRequestCount >= RESET_REQUEST_LIMIT_PER_HOUR) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many reset requests. Please try again later.");
        }

        PasswordResetRequest resetRequest = new PasswordResetRequest();
        resetRequest.setEmail(email);
        resetRequest.setRequestedAt(now);
        passwordResetRequestRepository.save(resetRequest);

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Generic success response is handled in controller to avoid account enumeration.
            return;
        }

        User user = userOpt.get();
        if (!StringUtils.hasText(user.getEmail())) {
            // Keep generic behavior and do not leak user existence details.
            return;
        }

        String rawToken = generateRawToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(sha256(rawToken));
        token.setExpiresAt(now.plusMinutes(RESET_TOKEN_TTL_MINUTES));
        token.setUsed(false);
        passwordResetTokenRepository.save(token);

        emailService.sendPasswordResetToken(user.getEmail(), rawToken, token.getExpiresAt());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        validateConfirmPassword(request.getNewPassword(), request.getConfirmPassword());
        validatePasswordPolicy(request.getNewPassword());

        String tokenValue = request.getToken() == null ? null : request.getToken().trim();
        if (!StringUtils.hasText(tokenValue)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reset link");
        }

        String tokenHash = sha256(tokenValue);
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reset link"));
        User user = token.getUser();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reset link");
        }

        LocalDateTime now = LocalDateTime.now();
        if (token.isUsed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reset link already used");
        }
        if (token.getExpiresAt().isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reset link expired");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(now);
        userRepository.save(user);
        refreshTokenService.revokeByUser(user);

        token.setUsed(true);
        token.setUsedAt(now);
        passwordResetTokenRepository.save(token);

        List<PasswordResetToken> remainingTokens = passwordResetTokenRepository.findAllByUser_IdAndUsedFalse(user.getId());
        for (PasswordResetToken remainingToken : remainingTokens) {
            if (!remainingToken.getId().equals(token.getId())) {
                remainingToken.setUsed(true);
                remainingToken.setUsedAt(now);
            }
        }
        passwordResetTokenRepository.saveAll(remainingTokens);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(String currentUsername) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication token"));
        return toAccountResponse(user);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(String currentUsername, Long requestedUserId) {
        User requester = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication token"));

        if (!Objects.equals(requester.getId(), requestedUserId) && !canReadCustomerProfiles(requester)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        User requestedUser = userRepository.findById(requestedUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return toAccountResponse(requestedUser);
    }

    @Transactional
    public AccountResponse updateAccount(String currentUsername, UpdateAccountRequest request) {
        return updateAccount(
                currentUsername,
                request.getFullName(),
                request.getPhone(),
                request.getAvatarUrl(),
                request.getAddress(),
                request.getDateOfBirth()
        );
    }

    @Transactional
    public AccountResponse updateAccount(String currentUsername, UpdateAccountMultipartRequest request) {
        String avatarUrl = null;
        if (request.getAvatarFile() != null && !request.getAvatarFile().isEmpty()) {
            avatarUrl = avatarStorageService.storeAvatar(request.getAvatarFile());
        }
        return updateAccount(
                currentUsername,
                request.getFullName(),
                request.getPhone(),
                avatarUrl,
                request.getAddress(),
                request.getDateOfBirth()
        );
    }

    private AccountResponse updateAccount(
            String currentUsername,
            String rawFullName,
            String rawPhone,
            String rawAvatarUrl,
            String rawAddress,
            LocalDate dateOfBirth) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication token"));

        if (user.getStatus() == UserStatus.LOCKED || user.getStatus() == UserStatus.INACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update a locked or inactive account");
        }

        String fullName = rawFullName.trim();
        String phone = rawPhone.trim();
        String avatarUrl = normalizeOptionalText(rawAvatarUrl);
        String address = normalizeOptionalText(rawAddress);

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid phone format");
        }
        if (fullName.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name too long");
        }
        if (avatarUrl != null && avatarUrl.length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar URL too long");
        }
        if (address != null && address.length() > ADDRESS_MAX_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Address too long");
        }
        if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date of birth cannot be in the future");
        }

        user.setFullName(fullName);
        user.setPhone(phone);
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }
        user.setAddress(address);
        user.setDateOfBirth(dateOfBirth);
        User savedUser = userRepository.saveAndFlush(user);
        return toAccountResponse(savedUser);
    }

    private AccountResponse toAccountResponse(User user) {
        Set<String> roleNames = user.getRoles() == null
                ? Collections.emptySet()
                : user.getRoles().stream()
                .filter(Objects::nonNull)
                .map(r -> r.getName())
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        String status = user.getStatus() == null ? null : user.getStatus().name();
        return new AccountResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getAddress(),
                user.getDateOfBirth(),
                user.getGender(),
                status,
                roleNames,
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private void validateConfirmPassword(String newPassword, String confirmPassword) {
        if (!StringUtils.hasText(newPassword) || !StringUtils.hasText(confirmPassword) || !newPassword.equals(confirmPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }
    }

    private void validatePasswordPolicy(String password) {
        if (!PASSWORD_POLICY.matcher(password).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must be at least 8 characters and include uppercase, lowercase, number, and special character"
            );
        }
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String normalizeEmail(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private boolean canReadCustomerProfiles(User user) {
        return hasAnyRole(user, "ADMIN", "ROLE_ADMIN", "MANAGER", "ROLE_MANAGER")
                || hasAnyPermission(user, "CUSTOMER:READ", "CUSTOMER_READ", "USER_VIEW");
    }

    private boolean hasAnyRole(User user, String... roleNames) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return false;
        }

        Set<String> expected = Set.of(roleNames);
        return user.getRoles().stream()
                .filter(Objects::nonNull)
                .map(role -> role.getName())
                .filter(StringUtils::hasText)
                .anyMatch(expected::contains);
    }

    private boolean hasAnyPermission(User user, String... permissionNames) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return false;
        }

        Set<String> expected = Set.of(permissionNames);
        return user.getRoles().stream()
                .filter(Objects::nonNull)
                .flatMap(role -> role.getPermissions() == null ? java.util.stream.Stream.empty() : role.getPermissions().stream())
                .filter(Objects::nonNull)
                .map(permission -> permission.getName())
                .filter(StringUtils::hasText)
                .anyMatch(expected::contains);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", e);
        }
    }
}
