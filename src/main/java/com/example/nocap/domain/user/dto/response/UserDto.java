package com.example.nocap.domain.user.dto.response;

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
    private String userType;
    private int point;
}
