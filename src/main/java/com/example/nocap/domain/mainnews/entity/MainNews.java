package com.example.nocap.domain.mainnews.entity;

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
public class MainNews {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long newsId;

    @OneToOne
    @JoinColumn(name = "AnalysisID", nullable = false)
    private Analysis analysis;

    private String url;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String image;
}