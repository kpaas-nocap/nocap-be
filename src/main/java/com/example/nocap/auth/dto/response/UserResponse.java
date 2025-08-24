package com.example.nocap.auth.dto.response;

import com.example.nocap.domain.user.entity.User;
import lombok.Getter;

@Getter
public class UserResponse {
    private Long id;
    private String nickname;
    private boolean isSigned;

    public UserResponse(User user, boolean isSigned) {
        this.id = user.getId();
        this.nickname = user.getUsername();
        this.isSigned = isSigned;
    }
}