package com.group1.auth_service.security;

import com.group1.auth_service.entity.Permission;
import com.group1.auth_service.entity.Role;
import com.group1.auth_service.entity.User;
import com.group1.auth_service.entity.UserStatus;
import com.group1.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Canonical permission string for APIs: {@code RESOURCE:ACTION} (uppercase), from DB columns.
     */
    static String authorityFromResourceAndAction(Permission permission) {
        if (permission == null || !StringUtils.hasText(permission.getResource())
                || !StringUtils.hasText(permission.getAction())) {
            return null;
        }
        String resource = permission.getResource().trim().toUpperCase(Locale.ROOT);
        String action = permission.getAction().trim().toUpperCase(Locale.ROOT);
        return resource + ":" + action;
    }

    private static void addPermissionAuthorities(Set<GrantedAuthority> authorities, Permission permission) {
        if (permission == null) {
            return;
        }
        String resourceAction = authorityFromResourceAndAction(permission);
        if (resourceAction != null) {
            authorities.add(new SimpleGrantedAuthority(resourceAction));
        }
        // Legacy: permission.name (e.g. USER_VIEW) for existing @PreAuthorize strings
        if (StringUtils.hasText(permission.getName())) {
            authorities.add(new SimpleGrantedAuthority(permission.getName().trim()));
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Set<GrantedAuthority> authorities = new HashSet<>();

        // 🔹 Loop tất cả roles
        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {

                if (role == null)
                    continue;

                // 🔹 Add ROLE (name in DB may already be "ROLE_XXX" or "XXX")
                if (role.getName() != null) {
                    String authority = role.getName().startsWith("ROLE_") ? role.getName() : "ROLE_" + role.getName();
                    authorities.add(new SimpleGrantedAuthority(authority));
                }

                // 🔹 Add PERMISSIONS (RESOURCE:ACTION + legacy name)
                if (role.getPermissions() != null) {
                    for (Permission permission : role.getPermissions()) {
                        addPermissionAuthorities(authorities, permission);
                    }
                }
            }
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)

                // true = account bị khóa
                .accountLocked(user.getStatus() == UserStatus.LOCKED)

                // true = account bị disable
                .disabled(user.getStatus() == UserStatus.INACTIVE)

                .build();
    }
}