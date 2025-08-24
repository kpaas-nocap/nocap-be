package com.example.nocap.auth.support;

import com.example.nocap.auth.dto.response.UserDetail;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthContext {
    private AuthContext() {}
    public static Long currentUserPk() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated()) return null;
        Object p = a.getPrincipal();
        if (p instanceof UserDetail ud) return ud.getId();
        return null;
    }
}