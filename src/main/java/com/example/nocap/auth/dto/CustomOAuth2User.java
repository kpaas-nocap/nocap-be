package com.example.nocap.auth.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {
    private final UserDto user;

    public CustomOAuth2User(UserDto user) {
        this.user = user;
    }

    @Override
    public Map<String,Object> getAttributes() {
        return Collections.emptyMap();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole()));
    }

    @Override
    public String getName() {
        return user.getUserId();
    }
}