package com.example.nocap.auth.config;

import com.example.nocap.auth.jwt.JWTFilter;
import com.example.nocap.auth.jwt.JwtUtil;
import com.example.nocap.auth.jwt.LoginFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
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
    private final AuthenticationConfiguration authenticationConfiguration;

    private final JwtUtil jwtUtil;

    @Bean
    public PasswordEncoder passwordEncoder() {
       return new BCryptPasswordEncoder();
   }

   @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
       return configuration.getAuthenticationManager();
   }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        // 2) CORS (WebMvcConfigurer 전역 설정 사용)
        http.cors(Customizer.withDefaults());

        // 3) 인증·인가 규칙
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/api/nocap/auth/signup",
                        "/api/nocap/auth/login" , // 로그인 엔드포인트
                        "**"
                ).permitAll()
                .anyRequest().authenticated()
        );

        // 4) 세션리스 (선택)
        http.sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // 5) 커스텀 로그인 필터 등록
        LoginFilter loginFilter = new LoginFilter(
                authenticationManager(authenticationConfiguration),
                jwtUtil
        );
        loginFilter.setFilterProcessesUrl("/api/nocap/auth/login");
        http.addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

        // 6) JWT 검증 필터 등록 (로그인 필터 이후)
        http.addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
