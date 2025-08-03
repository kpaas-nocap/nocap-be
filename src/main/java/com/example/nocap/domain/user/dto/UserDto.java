package com.example.nocap.domain.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDto {
    private Long Id;
    private String userId;
    private String username;
    private String role;
}
