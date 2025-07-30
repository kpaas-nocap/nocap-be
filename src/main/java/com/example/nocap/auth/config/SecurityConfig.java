package com.example.nocap.auth.config;

import com.example.nocap.auth.jwt.JWTFilter;
import com.example.nocap.auth.jwt.JwtUtil;
import com.example.nocap.auth.jwt.LoginFilter;
import com.example.nocap.auth.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 1) AuthenticationManager는 AuthenticationConfiguration으로부터 단일 빈으로 취득
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 2) SecurityFilterChain 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationManager authManager) throws Exception {

        // LoginFilter 세팅
        LoginFilter loginFilter = new LoginFilter(authManager, jwtUtil);
        loginFilter.setFilterProcessesUrl("/api/nocap/auth/login");

        // JWTFilter 세팅
        JWTFilter jwtFilter = new JWTFilter(jwtUtil) {
            @Override
            protected boolean shouldNotFilter(HttpServletRequest request) {
                String path = request.getServletPath();
                // 로그인·회원가입 요청은 JWT 검증 스킵
                return "/api/nocap/auth/login".equals(path)
                        || "/api/nocap/auth/signup".equals(path);
            }
        };

        http
                .csrf(cs -> cs.disable())
                .formLogin(fl -> fl.disable())
                .httpBasic(b -> b.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/nocap/auth/signup", "/api/nocap/auth/login")
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                )
                // 3) 단일 AuthenticationManager 설정 (기본 프로바이더 사용)
                .authenticationManager(authManager)
                // 4) 필터 순서 조정
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}