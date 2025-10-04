package com.example.nocap.auth.controller;

import com.example.nocap.auth.dto.request.IssueTempPasswordRequest;
import com.example.nocap.auth.service.PasswordService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/password")
public class PasswordController {
    private final PasswordService passwordService;

    @PostMapping("/issue-temp")
    public ResponseEntity<Void> issueTemp(@RequestBody IssueTempPasswordRequest req) {
        passwordService.issueTempPasswordByUserId(req.getUserId());
        return ResponseEntity.ok().build();
    }
}