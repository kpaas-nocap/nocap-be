package com.example.nocap.domain.analysis.entity;

import com.example.nocap.domain.bookmark.entity.Bookmark;
import com.example.nocap.domain.comment.entity.Comment;
import com.example.nocap.domain.mainnews.entity.MainNews;
import com.example.nocap.domain.news.entity.News;
import com.example.nocap.domain.user.entity.User;
import com.example.nocap.domain.useranalysis.entity.UserAnalysis;
import jakarta.persistence.*;
import java.util.ArrayList;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"mainNews", "relatedNews", "comments", "userAnalyses", "bookmarks"})
public class Analysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long analysisId;

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserAnalysis> userAnalyses = new ArrayList<>();

    private String category;

    private Long view;

    private LocalDateTime date;
  
    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL)
    private List<News> relatedNews;

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Bookmark> bookmarks = new ArrayList<>();

    @OneToOne(mappedBy = "analysis", cascade = CascadeType.ALL)
    private MainNews mainNews;

    private String plan; // 비교가 일반인지 프리미엄인지에 대한 상태

    public void setMainNews(MainNews mainNews) {
        this.mainNews = mainNews;
        mainNews.setAnalysis(this);
    }

    public void setRelatedNews(List<News> relatedNews) {
        this.relatedNews = relatedNews;
        relatedNews.forEach(news -> news.setAnalysis(this));
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
        comment.setAnalysis(this);
    }
}