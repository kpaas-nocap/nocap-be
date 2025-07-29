package com.example.nocap.auth.controller;

import com.example.nocap.auth.dto.SignupDto;
import com.example.nocap.auth.service.AuthService;
import com.example.nocap.domain.user.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nocap/auth")
public class AuthController {
    private final AuthService authservice;

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@RequestBody SignupDto signupDto){
        UserDto signed = authservice.signUp(signupDto);
        return ResponseEntity.ok(signed);
    }

}
