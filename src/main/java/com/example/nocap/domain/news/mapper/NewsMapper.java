package com.example.nocap.domain.news.mapper;

import com.example.nocap.domain.analysis.dto.SbertResponseDto;
import com.example.nocap.domain.analysis.dto.SbertResponseDto.NewsComparisonDto;
import com.example.nocap.domain.analysis.dto.SbertResponseDto.NewsWithSimilarityDto;
import com.example.nocap.domain.news.dto.NewsRequestDto;
import com.example.nocap.domain.news.dto.NewsResponseDto;
import com.example.nocap.domain.news.entity.News;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface NewsMapper {
    // 필드명이 같으면 자동으로 매핑
    @Named("Simple")
    @Mapping(source = "news", target = "newsWithSimilarityDto") // 1. newsWithSimilarityDto 필드는 news 객체 전체를 사용
    @Mapping(source = "comparison", target = "comparison")     // 2. comparison 필드는 이름이 같으니 그대로 매핑
    NewsComparisonDto toNewsComparisonDto(News news);

    @Mapping(source = "news", target = "newsDto")           // 3. newsDto 필드는 news 객체 전체를 사용
    @Mapping(source = "similarity", target = "similarity") // 4. similarity 필드는 이름이 같으니 그대로 매핑
    NewsWithSimilarityDto toNewsWithSimilarityDto(News news);

    NewsRequestDto toNewsRequestDto(News news);
    NewsResponseDto toNewsResponseDto(News news);

    List<NewsRequestDto> toNewsRequestDtoList(List<News> newsList);


    @Named("Sbert")
    @Mapping(source = "similarity", target = "newsWithSimilarityDto.similarity")
    @Mapping(source = ".", target = "newsWithSimilarityDto.newsDto")
    @Mapping(source = "comparison", target = "comparison")
    SbertResponseDto.NewsComparisonDto toSbertNewsComparisonDto(News news);

    // SBERT 응답 DTO -> News Entity 변환
    @Mapping(source = "newsWithSimilarityDto.newsDto.url", target = "url")
    @Mapping(source = "newsWithSimilarityDto.newsDto.title", target = "title")
    @Mapping(source = "newsWithSimilarityDto.newsDto.content", target = "content")
    @Mapping(source = "newsWithSimilarityDto.newsDto.date", target = "date")
    @Mapping(source = "newsWithSimilarityDto.newsDto.phrases", target = "phrases")
    @Mapping(source = "newsWithSimilarityDto.similarity", target = "similarity")
    @Mapping(source = "comparison", target = "comparison")
    @Mapping(target = "newsId", ignore = true) // DB에서 자동 생성되므로 매핑 제외
    @Mapping(target = "analysis", ignore = true) // 나중에 서비스에서 설정
    News sbertDtoToEntity(NewsComparisonDto dto);

    // SBERT 응답 DTO 리스트 -> News Entity 리스트 변환
    List<News> sbertDtoListToEntityList(List<NewsComparisonDto> dtoList);
}
