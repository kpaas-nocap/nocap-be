package com.example.nocap.domain.news.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewsRequestDto {

    private String url;

    private String title;

    private String date;

    @Column(columnDefinition = "TEXT")
    private String content;
}
