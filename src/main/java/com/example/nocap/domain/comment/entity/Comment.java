package com.example.nocap.domain.comment.entity;

import com.example.nocap.domain.analysis.entity.Analysis;
import com.example.nocap.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "AnalysisID", nullable = false)
    private Analysis analysis;

    @ManyToOne
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    private String content;

    private LocalDateTime date;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int recommendation = 0;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int nonRecommendation = 0;

    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private boolean reported = false;
}