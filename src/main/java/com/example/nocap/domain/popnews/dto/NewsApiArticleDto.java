package com.example.nocap.domain.popnews.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class NewsApiArticleDto {
    private String title;
    private String url;
}