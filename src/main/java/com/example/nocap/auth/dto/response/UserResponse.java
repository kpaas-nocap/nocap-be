package com.example.nocap.auth.dto.response;

import com.example.nocap.domain.user.entity.User;
import lombok.Getter;

@Getter
public class UserResponse {
    private Long id;
    private String nickname;
    private boolean isSigned;

    // user가 null일 수 있는 경우(미가입 상태) 처리
    public UserResponse(User user, boolean isSigned) {
        if (user != null) {
            this.id = user.getId();
            this.nickname = user.getUsername();
        } else {
            this.id = null;
            this.nickname = null;
        }
        this.isSigned = isSigned;
    }
}