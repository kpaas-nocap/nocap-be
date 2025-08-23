package com.example.nocap.domain.analysis.entity;

import com.example.nocap.domain.comment.entity.Comment;
import com.example.nocap.domain.mainnews.entity.MainNews;
import com.example.nocap.domain.news.entity.News;
import com.example.nocap.domain.user.entity.User;
import jakarta.persistence.*;
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

    @ManyToOne
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    private String category;

    private Long view;

    private LocalDateTime date;

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL)
    private List<Comment> comments;

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL)
    private List<News> relatedNews;

    @OneToOne(mappedBy = "analysis", cascade = CascadeType.ALL)
    private MainNews mainNews;

    public void setMainNews(MainNews mainNews) {
        this.mainNews = mainNews;
        mainNews.setAnalysis(this);
    }

    public void setRelatedNews(List<News> relatedNews) {
        this.relatedNews = relatedNews;
        relatedNews.forEach(news -> news.setAnalysis(this));
    }
}