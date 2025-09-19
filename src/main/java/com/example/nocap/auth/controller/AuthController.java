package com.example.nocap.auth.controller;


import com.example.nocap.auth.dto.request.FormLoginRequest;
import com.example.nocap.auth.dto.request.FormSignupRequest;
import com.example.nocap.auth.dto.request.SignupRequest;
import com.example.nocap.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController implements AuthSwagger{

    private final AuthService authService;

    @Value("${spring.kakao.auth.client}")
    private String clientId;

    @Value("${spring.kakao.auth.redirect}")
    private String redirectUri;

    @GetMapping("kakao/login")
    public ResponseEntity<String> getKakaoLoginUrl() {
        String kakaoLoginUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri;

        return ResponseEntity.ok(kakaoLoginUrl);
    }

    @PostMapping("kakao/signup")
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

    @PostMapping("/form/signup")
    public ResponseEntity<?> formSignup(@RequestBody FormSignupRequest req, HttpServletResponse httpServletResponse) {
        return ResponseEntity.ok(authService.formSignup(req, httpServletResponse));
    }

    @PostMapping("/form/login")
    public ResponseEntity<?> formLogin(@RequestBody FormLoginRequest req, HttpServletResponse httpServletResponse) {
        return ResponseEntity.ok(authService.formLogin(req, httpServletResponse));
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        authService.logout(authorization);
        return ResponseEntity.noContent().build();
    }
}