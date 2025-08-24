package com.example.nocap.auth.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FormSignupRequest {
    private String userId;
    private String username;
    private String password;
}