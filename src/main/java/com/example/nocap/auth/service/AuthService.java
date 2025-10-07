package com.example.nocap.auth.service;

import com.example.nocap.auth.dto.request.FormLoginRequest;
import com.example.nocap.auth.dto.request.FormSignupRequest;
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

import java.util.Date;
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
    private final TokenBlacklist tokenBlacklist;

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
            httpServletResponse.setHeader("Authorization", "Bearer " +token);
            log.info("token: {}", token);
            return new UserResponse(user, true);
        }

        String tmptoken = jwtUtil.createPreRegisterToken(email, 60 * 60 * 1000L);
        httpServletResponse.setHeader("Authorization", "Bearer " + tmptoken);
        log.info("tmptoken: {}", tmptoken);
        return new UserResponse(null, false);
    }

    public UserResponse signup(SignupRequest signupRequest, String token, HttpServletResponse httpServletResponse) {
        String email = jwtUtil.getUserId(token); // Email → userId

        if (userRepository.existsByUserId(email)) throw new RuntimeException("이미 존재합니다.");

        User newUser = User.builder()
                .userId(email)
                .username(signupRequest.getNickname())
                .userPw(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role("USER")
                .build();

        userRepository.save(newUser);

        String newToken = jwtUtil.createJwt(newUser, 60 * 60 * 1000L);
        httpServletResponse.setHeader("Authorization", "Bearer " +newToken);

        return new UserResponse(newUser, true);
    }

    public UserResponse formSignup(FormSignupRequest req, HttpServletResponse httpServletResponse) {
        if (userRepository.existsByUserId(req.getUserId())) throw new RuntimeException("이미 존재합니다.");
        User newUser = User.builder()
                .userId(req.getUserId())
                .username(req.getUsername())
                .userPw(passwordEncoder.encode(req.getPassword()))
                .role("USER")
                .build();
        userRepository.save(newUser);
        String token = jwtUtil.createJwt(newUser, 60 * 60 * 1000L);
        httpServletResponse.setHeader("Authorization", "Bearer " + token);
        return new UserResponse(newUser, true);
    }

    public UserResponse formLogin(FormLoginRequest req, HttpServletResponse httpServletResponse) {
        User user = userRepository.findByUserId(req.getUserId()).orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));
        if (!passwordEncoder.matches(req.getPassword(), user.getUserPw())) throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        String token = jwtUtil.createJwt(user, 60 * 60 * 1000L);
        httpServletResponse.setHeader("Authorization", "Bearer " + token);
        return new UserResponse(user, true);
    }
    public void logout(String authorization) {
        String token = authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
        Date exp = jwtUtil.getExpiration(token);
        tokenBlacklist.blacklist(token, exp.getTime());
    }
}