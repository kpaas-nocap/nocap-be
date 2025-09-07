package com.example.nocap.domain.user.entity;

import com.example.nocap.auth.dto.SignupDto;
import com.example.nocap.domain.analysis.entity.Analysis;
import com.example.nocap.domain.bookmark.entity.Bookmark;
import com.example.nocap.domain.comment.entity.Comment;
import com.example.nocap.domain.history.entity.History;
import com.example.nocap.domain.useranalysis.entity.UserAnalysis;
import jakarta.persistence.*;
import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAnalysis> userAnalyses = new ArrayList<>();

    public static User from (SignupDto dto){
        return User.builder()
                .userId(dto.getUserId())
                .userPw(dto.getUserPw())
                .username(dto.getUsername())
                .role("USER")
                .build();
    }
}
