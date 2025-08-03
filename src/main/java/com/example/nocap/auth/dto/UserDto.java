package com.example.nocap.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class UserDto {
    private String userId;
    private String username;
    private String role;
}
