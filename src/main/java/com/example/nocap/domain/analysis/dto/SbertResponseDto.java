package com.example.nocap.domain.analysis.dto;

import com.example.nocap.domain.mainnews.dto.MainNewsResponseDto;
import com.example.nocap.domain.news.dto.NewsRequestDto;
import com.example.nocap.domain.news.dto.NewsResponseDto;
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
    private MainNewsResponseDto mainNewsDto;
    private List<NewsComparisonDto> newsComparisonDtos = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewsComparisonDto {
        private NewsWithSimilarityDto newsWithSimilarityDto;
        private String comparison;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewsWithSimilarityDto {
        private double similarity;
        private NewsResponseDto newsDto;
    }
}