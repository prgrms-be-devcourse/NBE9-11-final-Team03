package com.back.baton.global.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class SecurityUser implements UserDetails {

    private final Long userId;
    private final String role;
    private final List<GrantedAuthority> authorities;

    private SecurityUser(Long userId, String role) {
        this.userId = userId;
        this.role = role;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    public static SecurityUser of(Long userId, String role) {
        return new SecurityUser(userId, role);
    }

    public Long getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return userId.toString();
    }
}
