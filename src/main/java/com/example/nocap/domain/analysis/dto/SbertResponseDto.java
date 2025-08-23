package com.example.nocap.domain.analysis.dto;

import com.example.nocap.domain.mainnews.dto.MainNewsDto;
import com.example.nocap.domain.news.dto.NewsDto;
import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SbertResponseDto {

    private String category;
    private MainNewsDto mainNewsDto;
    private List<NewsComparisonDto> newsComparisonDtos = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewsComparisonDto {
        private NewsWithSimilarityDto newsWithSimilarityDto;
        private String comparison; // Premium 플랜에서 제공하는 GPT-4 비교 요약문
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewsWithSimilarityDto {
        private double similarity;
        private NewsDto newsDto;
    }
}