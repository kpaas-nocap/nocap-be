package com.example.nocap.domain.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangepasswordRequest {
    private String currentPassword;
    private String newPassword;
}
