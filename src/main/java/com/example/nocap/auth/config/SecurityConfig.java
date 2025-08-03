package com.example.nocap.auth.config;

import com.example.nocap.auth.jwt.JWTFilter;
import com.example.nocap.auth.jwt.JwtUtil;
import com.example.nocap.auth.jwt.LoginFilter;
import com.example.nocap.auth.oauth2.CustomSuccessHandler;
import com.example.nocap.auth.service.CustomOAuth2UserService;
import com.example.nocap.auth.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final CustomOAuth2UserService oauth2UserService;
    private final CustomSuccessHandler successHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationManager authManager) throws Exception {

        // 1) 폼로그인 POST 처리 필터
        LoginFilter loginFilter = new LoginFilter(authManager, jwtUtil);
        loginFilter.setFilterProcessesUrl("/api/nocap/auth/login");

        // 2) JWT 검증 필터
        JWTFilter jwtFilter = new JWTFilter(jwtUtil) {
            @Override
            protected boolean shouldNotFilter(HttpServletRequest req) {
                String p = req.getServletPath();
                return p.startsWith("/api/nocap/auth/")       // login/signup
                        || p.startsWith("/oauth2/authorization/") // 카카오 인가 시작
                        || p.startsWith("/api/v1/oauth2/");       // 콜백
            }
        };

        http
                // REST API 모드: UI 리다이렉트 전부 끔
                .csrf(cs -> cs.disable())
                .formLogin(fl -> fl.disable())
                .httpBasic(b -> b.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                // 커스텀 필터
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                // OAuth2 로그인: 인가 요청만 302로 던지고, 실패/인증 필요 시 401
                .oauth2Login(oauth2 -> oauth2
                        // 302 시작 URL
                        .authorizationEndpoint(authz ->
                                authz.baseUri("/oauth2/authorization"))
                        // 카카오가 돌아오는 콜백 URL
                        .redirectionEndpoint(redir ->
                                redir.baseUri("/api/v1/oauth2/*"))
                        .userInfoEndpoint(u -> u.userService(oauth2UserService))
                        .successHandler(successHandler)
                        .failureHandler((req, res, ex) ->
                                res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                )

                // 인증 실패(로그인 안 된 상태로 보호된 API 호출) 시 401 리턴
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, authEx) ->
                                res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                )

                // 허용할 API
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/api/nocap/auth/signup",
                                "/api/nocap/auth/login",
                                "/oauth2/authorization/**",
                                "/api/v1/oauth2/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
        ;

        return http.build();
    }
}