package com.example.nocap.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDto {
    private Long Id;
    private String userId;
    private String username;
}
