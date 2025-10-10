package com.example.nocap.auth.config;


import com.example.nocap.auth.dto.response.UserDetail;
import com.example.nocap.auth.service.TokenBlacklist;
import com.example.nocap.domain.user.entity.User;
import com.example.nocap.domain.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final TokenBlacklist tokenBlacklist;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. 헤더에서 토큰을 가져옴 (없으면 null)
        String token = jwtUtil.resolveToken(request);

        // 2. 토큰이 존재하고 유효한 경우에만 인증 처리
        if (StringUtils.hasText(token) && !tokenBlacklist.isBlacklisted(token)) {
            try {
                if (!jwtUtil.isExpired(token)) {
                    String userId = jwtUtil.getUserId(token);
                    User user = userRepository.findByUserId(userId).orElse(null);

                    if (user != null) {
                        // isTmp 토큰이 아닌 경우에만 최종 인증 처리
                        boolean isTmp = jwtUtil.isTmp(token);
                        if (!isTmp) {
                            UserDetail userDetails = new UserDetail(user);
                            Authentication authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                        }
                    }
                }
            } catch (JwtException | IllegalArgumentException e) {
                // 토큰 파싱/검증 과정에서 오류가 발생해도 무시하고 다음 필터로 진행
                log.error("Invalid JWT Token: {}", e.getMessage());
            }
        }

        // 3. 토큰이 없거나 유효하지 않더라도, 예외를 던지지 않고 항상 다음 필터로 진행
        filterChain.doFilter(request, response);
    }
}