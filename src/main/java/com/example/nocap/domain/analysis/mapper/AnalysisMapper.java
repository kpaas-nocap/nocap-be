package com.example.nocap.domain.analysis.mapper;

import com.example.nocap.domain.analysis.dto.AnalysisDto;
import com.example.nocap.domain.analysis.dto.AnalysisViewDto;
import com.example.nocap.domain.analysis.dto.SbertResponseDto;
import com.example.nocap.domain.analysis.entity.Analysis;
import com.example.nocap.domain.mainnews.mapper.MainNewsMapper;
import com.example.nocap.domain.news.entity.News;
import com.example.nocap.domain.news.mapper.NewsMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {MainNewsMapper.class, NewsMapper.class})
public interface AnalysisMapper {

    AnalysisMapper INSTANCE = Mappers.getMapper(AnalysisMapper.class);

    @Mapping(source = "mainNews.image", target = "image")
    @Mapping(source = "mainNews.title", target = "mainNewsTitle")
    AnalysisDto toAnalysisDto(Analysis analysis);

    @Mapping(source = "mainNews.title", target = "mainNewsTitle")
    @Mapping(source = "mainNews.image", target = "image")
    // 2. 객체 자체를 DTO로 매핑 (MainNewsMapper 사용)
    @Mapping(source = "mainNews", target = "mainNewsDto")
    // 3. 자식 리스트를 DTO 리스트로 매핑 (NewsMapper 사용)
    @Mapping(source = "relatedNews", target = "newsComparisonDtos", qualifiedByName = "Sbert")
    AnalysisViewDto toAnalysisViewDto(Analysis analysis);

    @Mapping(source = "mainNews", target = "mainNewsDto")
    @Mapping(source = "relatedNews", target = "newsComparisonDtos", qualifiedByName = "Sbert")
    SbertResponseDto toSbertResponseDto(Analysis analysis);
}