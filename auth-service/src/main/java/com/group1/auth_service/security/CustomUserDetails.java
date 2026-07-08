// package com.group1.auth_service.security;

// import com.group1.auth_service.entity.User;
// import com.group1.auth_service.entity.UserStatus;
// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.userdetails.UserDetails;

// import java.util.Collection;
// import java.util.List;

// public class CustomUserDetails implements UserDetails {

//     private User user;

//     public CustomUserDetails(User user) {
//         this.user = user;
//     }

//     @Override
//     public Collection<? extends GrantedAuthority> getAuthorities() {
//         return List.of(
//                 new SimpleGrantedAuthority(user.getRole().getName())
//         );
//     }

//     @Override
//     public String getPassword() {
//         return user.getPassword();
//     }

//     @Override
//     public String getUsername() {
//         return user.getUsername();
//     }

//     @Override
//     public boolean isAccountNonExpired() {
//         return true;
//     }

//     @Override
//     public boolean isAccountNonLocked() {
//         return true;
//     }

//     @Override
//     public boolean isCredentialsNonExpired() {
//         return true;
//     }

//     @Override
//     public boolean isEnabled() {
//         return user.getStatus() == UserStatus.ACTIVE;
//     }
// }