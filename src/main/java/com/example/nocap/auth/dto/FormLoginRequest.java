package com.example.nocap.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FormLoginRequest {
    private String userId;
    private String password;
}