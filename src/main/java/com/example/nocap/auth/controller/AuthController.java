package com.example.nocap.auth.controller;


import com.example.nocap.auth.dto.request.SignupRequest;
import com.example.nocap.auth.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Auth", description = "로그인 관련 API")
public class AuthController {

    private final AuthService authService;

    @Value("${spring.kakao.auth.client}")
    private String clientId;

    @Value("${spring.kakao.auth.redirect}")
    private String redirectUri;

    @GetMapping("/login")
    public ResponseEntity<String> getKakaoLoginUrl() {
        String kakaoLoginUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri;

        return ResponseEntity.ok(kakaoLoginUrl);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest,
                                    @RequestHeader("Authorization") String tmptoken,
                                    HttpServletResponse httpServletResponse
    ) {
        String token = tmptoken.replace("Bearer ", "").trim();
        return ResponseEntity.ok(authService.signup(signupRequest, token, httpServletResponse));
    }

    @GetMapping("/login/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestParam("code") String accessCode, HttpServletResponse httpServletResponse) {
        return ResponseEntity.ok(authService.oAuthLogin(accessCode, httpServletResponse));
    }
}