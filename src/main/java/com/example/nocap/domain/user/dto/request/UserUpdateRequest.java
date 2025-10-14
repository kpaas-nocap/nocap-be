package com.example.nocap.domain.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {
    private String userId;
    private String username;
    private String currentPassword;
    private String newPassword;
}
