package com.example.nocap.domain.news.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewsDto {

    private String url;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;
}
