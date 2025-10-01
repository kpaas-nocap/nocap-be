package com.example.nocap.domain.comment.entity;

import com.example.nocap.domain.analysis.entity.Analysis;
import com.example.nocap.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @ManyToOne
    @JoinColumn(name = "AnalysisID", nullable = false)
    private Analysis analysis;

    @ManyToOne
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    private String content;

    private LocalDateTime date;

    private int recommendation = 0;

    private int nonRecommendation = 0;
}