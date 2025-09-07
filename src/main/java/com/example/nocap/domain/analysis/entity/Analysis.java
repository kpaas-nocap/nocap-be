package com.example.nocap.domain.analysis.entity;

import com.example.nocap.domain.comment.entity.Comment;
import com.example.nocap.domain.mainnews.entity.MainNews;
import com.example.nocap.domain.news.entity.News;
import com.example.nocap.domain.user.entity.User;
import com.example.nocap.domain.useranalysis.entity.UserAnalysis;
import jakarta.persistence.*;
import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Analysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long analysisId;

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAnalysis> userAnalyses = new ArrayList<>();

    private String category;

    private Long view;

    private LocalDateTime date;
  
    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL)
    private List<Comment> comments;

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL)
    private List<News> relatedNews;

    @OneToOne(mappedBy = "analysis", cascade = CascadeType.ALL)
    private MainNews mainNews;

    private String version; // 비교가 일반인지 프리미엄인지에 대한 상태

    public void setMainNews(MainNews mainNews) {
        this.mainNews = mainNews;
        mainNews.setAnalysis(this);
    }

    public void setRelatedNews(List<News> relatedNews) {
        this.relatedNews = relatedNews;
        relatedNews.forEach(news -> news.setAnalysis(this));
    }
}