package com.example.nocap.domain.news.dto;

import jakarta.persistence.Column;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewsResponseDto {

    private String url;

    private String title;

    private String date;

    @Column(columnDefinition = "TEXT")
    private String content;

    private List<String> phrases;
}
