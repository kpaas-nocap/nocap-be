package com.example.nocap.domain.user.entity;

import com.example.nocap.auth.dto.SignupDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    public static User from (SignupDto dto){
        return User.builder()
                .userId(dto.getUserId())
                .userPw(dto.getUserPw())
                .username(dto.getUsername())
                .role("USER")
                .build();
    }
}
