package com.example.nocap.domain.analysis.service;

import com.example.nocap.domain.analysis.dto.SbertResponseDto;
import com.example.nocap.domain.analysis.dto.SbertResponseDto.NewsComparisonDto;
import com.example.nocap.domain.analysis.entity.Analysis;
import com.example.nocap.domain.analysis.repository.AnalysisRepository;
import com.example.nocap.domain.mainnews.entity.MainNews;
import com.example.nocap.domain.news.entity.News;
import com.example.nocap.domain.news.mapper.NewsMapper;
import com.example.nocap.domain.user.entity.User;
import com.example.nocap.domain.user.repository.UserRepository;
import com.example.nocap.domain.useranalysis.entity.UserAnalysis;
import com.example.nocap.domain.useranalysis.repository.UserAnalysisRepository;
import com.example.nocap.exception.CustomException;
import com.example.nocap.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalysisSaveService {

    private final UserRepository userRepository;
    private final AnalysisRepository analysisRepository;
    private final UserAnalysisRepository userAnalysisRepository;
    private final NewsMapper newsMapper;

    @Transactional
    public void saveAnalysisData(Long userId, MainNews mainNews, SbertResponseDto sbertResponseDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Analysis analysis = Analysis.builder()
            .category(sbertResponseDto.getCategory())
            .view(0L)
            .date(LocalDateTime.now())
            .version(user.getRole())
            .build();
        mainNews.setPhrases(sbertResponseDto.getMainNewsDto().getPhrases());
        analysis.setMainNews(mainNews);

        if (sbertResponseDto.getNewsComparisonDtos() != null) {
            List<News> relatedNewsList = newsMapper.sbertDtoListToEntityList(sbertResponseDto.getNewsComparisonDtos());

            // 부모-자식 관계 설정
            relatedNewsList.forEach(news -> news.setAnalysis(analysis));

            analysis.setRelatedNews(relatedNewsList);
        }

        Analysis savedAnalysis = analysisRepository.save(analysis);
        UserAnalysis userAnalysisLink = UserAnalysis.builder()
            .user(user)
            .analysis(savedAnalysis)
            .build();

        userAnalysisRepository.save(userAnalysisLink);
    }

    @Transactional
    public void updateAnalysisData(Analysis existingAnalysis, SbertResponseDto sbertResponseDto) {
        // 1. DTO 리스트를 URL을 key로 하는 Map으로 변환하여 검색 효율을 높임
        Map<String, NewsComparisonDto> resultMap = sbertResponseDto.getNewsComparisonDtos().stream()
            .collect(Collectors.toMap(
                // Key: NewsDto의 URL
                dto -> dto.getNewsWithSimilarityDto().getNewsDto().getUrl(),
                // Value: NewsComparisonDto 자기 자신
                Function.identity()
            ));

        // 2. 기존 News 엔티티들을 순회하며, Map에 있는 새 정보로 comparison 필드 업데이트
        existingAnalysis.getRelatedNews().forEach(news -> {
            SbertResponseDto.NewsComparisonDto newInfo = resultMap.get(news.getUrl());
            if (newInfo != null) {
                news.setComparison(newInfo.getComparison());
            }
        });

        // 3. 분석 버전을 PREMIUM으로 변경
        existingAnalysis.setVersion("PREMIUM");
    }
}