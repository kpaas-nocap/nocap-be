package com.example.nocap.auth.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueTempPasswordRequest {
    private String userId;
    public IssueTempPasswordRequest() {}
    public IssueTempPasswordRequest(String userId) {
        this.userId = userId;
    }
}
