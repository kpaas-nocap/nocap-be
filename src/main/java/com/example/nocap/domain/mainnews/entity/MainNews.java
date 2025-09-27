package com.example.nocap.domain.mainnews.entity;

import com.example.nocap.domain.analysis.entity.Analysis;
import com.example.nocap.global.StringListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import java.util.List;
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

    @Column(unique = true, nullable = false)
    private String canonicalUrl;

    private String url;

    private String title;

    private String date;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String image;

    @Column(columnDefinition = "LONGTEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> phrases;
}