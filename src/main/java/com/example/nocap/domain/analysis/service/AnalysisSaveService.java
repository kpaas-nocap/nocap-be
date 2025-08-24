package com.example.nocap.domain.analysis.service;

import com.example.nocap.domain.analysis.dto.SbertResponseDto;
import com.example.nocap.domain.analysis.entity.Analysis;
import com.example.nocap.domain.analysis.repository.AnalysisRepository;
import com.example.nocap.domain.mainnews.entity.MainNews;
import com.example.nocap.domain.news.dto.NewsDto;
import com.example.nocap.domain.news.entity.News;
import com.example.nocap.domain.user.entity.User;
import com.example.nocap.domain.user.repository.UserRepository;
import com.example.nocap.exception.CustomException;
import com.example.nocap.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalysisSaveService {

    private final UserRepository userRepository;
    private final AnalysisRepository analysisRepository;

    @Transactional
    public void saveAnalysisData(Long userId, MainNews mainNews, SbertResponseDto sbertResponseDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Analysis analysis = Analysis.builder()
            .user(user)
            .category(sbertResponseDto.getCategory())
            .view(0L)
            .date(LocalDateTime.now())
            .build();

        analysis.setMainNews(mainNews);

        if (sbertResponseDto.getNewsComparisonDtos() != null) {
            List<News> relatedNewsList = sbertResponseDto.getNewsComparisonDtos().stream()
                .map(compDto -> {
                    NewsDto newsDto = compDto.getNewsWithSimilarityDto().getNewsDto();
                    return News.builder()
                        .url(newsDto.getUrl())
                        .title(newsDto.getTitle())
                        .content(newsDto.getContent())
                        .similarity(compDto.getNewsWithSimilarityDto().getSimilarity())
                        .comparison(compDto.getComparison())
                        .build();
                })
                .collect(Collectors.toList());
            analysis.setRelatedNews(relatedNewsList);
        }

        analysisRepository.save(analysis);
    }
}