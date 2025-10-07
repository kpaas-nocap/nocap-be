package com.example.nocap.domain.analysis.mapper;

import com.example.nocap.domain.analysis.dto.AnalysisDto;
import com.example.nocap.domain.analysis.dto.AnalysisViewDto;
import com.example.nocap.domain.analysis.dto.SbertResponseDto;
import com.example.nocap.domain.analysis.entity.Analysis;
import com.example.nocap.domain.mainnews.mapper.MainNewsMapper;
import com.example.nocap.domain.news.mapper.NewsMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MainNewsMapper.class, NewsMapper.class})
public interface AnalysisMapper {

    // Analysis -> AnalysisDto
    @Mapping(source = "analysis.mainNews.image", target = "image")
    @Mapping(source = "analysis.mainNews.title", target = "mainNewsTitle")
    AnalysisDto toAnalysisDto(Analysis analysis);

    // Analysis -> AnalysisViewDto
    @Mapping(source = "analysis.mainNews.title", target = "mainNewsTitle")
    @Mapping(source = "analysis.mainNews.image", target = "image")
    @Mapping(source = "analysis.mainNews", target = "mainNewsDto")
    @Mapping(source = "analysis.relatedNews", target = "newsComparisonDtos", qualifiedByName = "Sbert")
    @Mapping(source = "isBookmarked", target = "bookmarked")
    AnalysisViewDto toAnalysisViewDto(Analysis analysis, boolean isBookmarked);

    // Analysis -> SbertResponseDto
    @Mapping(source = "analysis.mainNews", target = "mainNewsDto")
    @Mapping(source = "analysis.relatedNews", target = "newsComparisonDtos", qualifiedByName = "Sbert")
    SbertResponseDto toSbertResponseDto(Analysis analysis);
}