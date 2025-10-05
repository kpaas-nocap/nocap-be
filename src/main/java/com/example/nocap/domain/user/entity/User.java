package com.example.nocap.domain.user.entity;

import com.example.nocap.domain.useranalysis.entity.UserAnalysis;
import com.example.nocap.auth.dto.request.SignupRequest;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
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
    private int point;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAnalysis> userAnalyses = new ArrayList<>();


    public static User from (SignupRequest dto){

        return User.builder()
                .userId(dto.getNickname())
                .role("ROLE_USER")
                .build();
    }
}
