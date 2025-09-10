package com.example.nocap.domain.popnews.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class NewsApiResponseDto {
    private List<NewsApiArticleDto> articles;
}