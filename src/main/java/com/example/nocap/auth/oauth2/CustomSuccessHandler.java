package com.example.nocap.auth.oauth2;

import com.example.nocap.auth.dto.CustomOAuth2User;
import com.example.nocap.auth.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;

    public CustomSuccessHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication auth)
            throws IOException {
        CustomOAuth2User user = (CustomOAuth2User) auth.getPrincipal();
        String token = jwtUtil.createJwt(user.getName(),
                auth.getAuthorities().iterator().next().getAuthority(),
                60 * 60 * 1000L);

        Cookie cookie = new Cookie("Authorization", "Bearer" + token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60);
        res.addCookie(cookie);

        // 프론트 URL로 리다이렉트
        getRedirectStrategy().sendRedirect(req, res, "http://localhost:3000");
    }
}