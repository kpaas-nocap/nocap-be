package com.example.nocap.domain.analysis.service;

import com.example.nocap.domain.analysis.dto.SbertResponseDto;
import com.example.nocap.domain.analysis.entity.Analysis;
import com.example.nocap.domain.news.entity.News;
import com.example.nocap.domain.news.mapper.NewsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisUpdateService {

    private final NewsMapper newsMapper;

    @Transactional
    public void updateAnalysisData(Analysis analysis, String plan, SbertResponseDto sbertResponseDto) {

        // 1. Analysis 엔티티의 필드 업데이트
        analysis.setCategory(sbertResponseDto.getCategory());
        analysis.setPlan(plan);
        analysis.setDate(LocalDateTime.now()); // 분석 날짜 갱신

        // 2. MainNews의 phrases 업데이트
        analysis.getMainNews().setPhrases(sbertResponseDto.getMainNewsDto().getPhrases());

        // 3. 기존 relatedNews는 모두 삭제
        analysis.getRelatedNews().clear();

        // 4. 새로운 relatedNews 추가
        if (sbertResponseDto.getNewsComparisonDtos() != null) {
            List<News> newRelatedNews = newsMapper.sbertDtoListToEntityList(sbertResponseDto.getNewsComparisonDtos());
            newRelatedNews.forEach(news -> news.setAnalysis(analysis)); // 연관관계 설정
            analysis.getRelatedNews().addAll(newRelatedNews);
        }
    }
}
