package com.example.nocap.auth.jwt;

import com.example.nocap.auth.dto.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collection;
import java.util.Iterator;
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public LoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        super.setAuthenticationManager(authenticationManager);
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }
    @Override
    public Authentication attemptAuthentication(HttpServletRequest req,
                                                HttpServletResponse res)
            throws AuthenticationException {
        String username = req.getParameter("userId");
        String password = req.getParameter("userPw");
        System.out.println("[LoginFilter] attemptAuthentication → username=" + username + ", password=" + password);
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req,
                                            HttpServletResponse res,
                                            FilterChain chain,
                                            Authentication auth) {
        System.out.println("[LoginFilter] successfulAuthentication 호출됨");
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        String role = auth.getAuthorities().iterator().next().getAuthority();
        String token = jwtUtil.createJwt(user.getUsername(), role, jwtUtil.getExpirationMs());
        res.addHeader("Authorization", "Bearer " + token);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest req,
                                              HttpServletResponse res,
                                              AuthenticationException failed) {
        System.out.println("[LoginFilter] unsuccessfulAuthentication 호출됨: " + failed.getMessage());
        res.setStatus(401);
    }

}