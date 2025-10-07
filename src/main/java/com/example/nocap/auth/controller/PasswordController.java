package com.example.nocap.auth.controller;

import com.example.nocap.auth.dto.request.IssueTempPasswordRequest;
import com.example.nocap.auth.service.PasswordService;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/password")
public class PasswordController implements PasswordSwagger{
    private final PasswordService passwordService;

    @PostMapping("/issue-temp")
    public ResponseEntity<Void> issueTemp(@RequestBody IssueTempPasswordRequest req) {
        System.out.println("req.userId() => " + req.getUserId());
        passwordService.issueTempPasswordByUserId(req.getUserId());
        return ResponseEntity.ok().build();
    }
}