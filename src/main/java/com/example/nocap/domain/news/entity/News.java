package com.example.nocap.domain.news.entity;

import com.example.nocap.domain.analysis.entity.Analysis;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long newsId;

    @ManyToOne
    @JoinColumn(name = "AnalysisID", nullable = false)
    private Analysis analysis;

    private String url;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Double similarity;

    @Column(columnDefinition = "TEXT")
    private String comparison;
}