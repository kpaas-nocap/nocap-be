package com.example.nocap.auth.service;

import com.example.nocap.auth.kakao.KakaoUserInfoResponseDto;
import com.example.nocap.auth.kakao.KakaoUtil;
import com.example.nocap.auth.dto.request.SignupRequest;
import com.example.nocap.auth.dto.response.UserResponse;
import com.example.nocap.auth.config.JwtUtil;
import com.example.nocap.domain.user.entity.User;
import com.example.nocap.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
    private final KakaoUtil kakaoUtil;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public UserResponse oAuthLogin(String accessCode, HttpServletResponse httpServletResponse) {
        String accessToken = kakaoUtil.getAccessTokenFromKakao(accessCode);
        KakaoUserInfoResponseDto userInfo = kakaoUtil.getUserInfo(accessToken);
        log.info("카카오 accessToken={}", accessToken);
        log.info("카카오 userInfo email={}", userInfo.getKakaoAccount().getEmail());
        log.info("카카오 userInfo nickname={}", userInfo.getKakaoAccount().getProfile().getNickName());
        log.info("카카오 userInfo raw={}", userInfo); // 전체 객체
        String email = userInfo.getKakaoAccount().getEmail();
        Optional<User> optional = userRepository.findByUserId(email);

        if (optional.isPresent()) {
            User user = optional.get();
            String token = jwtUtil.createJwt(user, 60 * 60 * 1000L);
            httpServletResponse.setHeader("Authorization", token);
            log.info("token: {}", token);
            return new UserResponse(user, true);
        }

        String tmptoken = jwtUtil.createPreRegisterToken(email, 10 * 60 * 1000L);
        httpServletResponse.setHeader("Authorization", tmptoken);
        log.info("tmptoken: {}", tmptoken);
        return new UserResponse(null, false);
    }

    public UserResponse signup(SignupRequest signupRequest, String token, HttpServletResponse httpServletResponse) {
        String email = jwtUtil.getUsername(token); // Email → userId

        if (userRepository.existsByUserId(email)) throw new RuntimeException("이미 존재합니다.");

        User newUser = User.builder()
                .userId(email)
                .username(signupRequest.getNickname())
                .userPw(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role("ROLE_USER")
                .build();

        userRepository.save(newUser);

        String newToken = jwtUtil.createJwt(newUser, 60 * 60 * 1000L);
        httpServletResponse.setHeader("Authorization", newToken);

        return new UserResponse(newUser, true);
    }
}