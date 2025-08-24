package com.example.nocap.domain.news.mapper;

import com.example.nocap.domain.analysis.dto.SbertResponseDto.NewsComparisonDto;
import com.example.nocap.domain.analysis.dto.SbertResponseDto.NewsWithSimilarityDto;
import com.example.nocap.domain.news.dto.NewsDto;
import com.example.nocap.domain.news.entity.News;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NewsMapper {
    // 필드명이 같으면 자동으로 매핑
    @Mapping(source = "news", target = "newsWithSimilarityDto") // 1. newsWithSimilarityDto 필드는 news 객체 전체를 사용
    @Mapping(source = "comparison", target = "comparison")     // 2. comparison 필드는 이름이 같으니 그대로 매핑
    NewsComparisonDto toNewsComparisonDto(News news);

    @Mapping(source = "news", target = "newsDto")           // 3. newsDto 필드는 news 객체 전체를 사용
    @Mapping(source = "similarity", target = "similarity") // 4. similarity 필드는 이름이 같으니 그대로 매핑
    NewsWithSimilarityDto toNewsWithSimilarityDto(News news);

    NewsDto toNewsDto(News news);
}
