package com.example.nocap.domain.user.entity;

import com.example.nocap.auth.dto.request.SignupRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String userPw;
    private String username;
    private String role;

    public static User from (SignupRequest dto){
        return User.builder()
                .userId(dto.getNickname())
                .role("USER")
                .build();
    }
}
