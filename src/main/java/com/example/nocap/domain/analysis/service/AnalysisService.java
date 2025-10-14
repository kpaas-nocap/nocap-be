package com.example.nocap.domain.analysis.service;

import com.example.nocap.auth.dto.response.UserDetail;
import com.example.nocap.domain.analysis.dto.AnalysisViewDto;
import com.example.nocap.domain.analysis.dto.IsAnalyzedDto;
import com.example.nocap.domain.analysis.mapper.AnalysisMapper;
import com.example.nocap.domain.analysis.dto.AnalysisDto;
import com.example.nocap.domain.analysis.entity.Analysis;
import com.example.nocap.domain.analysis.repository.AnalysisRepository;
import com.example.nocap.domain.bookmark.repository.BookmarkRepository;
import com.example.nocap.domain.mainnews.entity.MainNews;
import com.example.nocap.domain.mainnews.repository.MainNewsRepository;
import com.example.nocap.domain.user.entity.User;
import com.example.nocap.domain.user.repository.UserRepository;
import com.example.nocap.domain.useranalysis.entity.UserAnalysis;
import com.example.nocap.exception.CustomException;
import com.example.nocap.exception.ErrorCode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final AnalysisMapper analysisMapper;
    private final UserRepository userRepository;
    private final MainNewsRepository mainNewsRepository;
    private final UrlNormalizationService urlNormalizationService;
    private final BookmarkRepository bookmarkRepository;

    private static final Logger log = LoggerFactory.getLogger(AnalysisProcessService.class);


    @Transactional(readOnly = true)
    public List<AnalysisDto> getAllAnalysis() {
        return analysisRepository.findAll().stream()
            .map(analysisMapper::toAnalysisDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public AnalysisViewDto getAnalysisById(Long id, UserDetail userDetail) {
        Analysis analysis = analysisRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.ANALYSIS_NOT_FOUND));

        boolean isBookmarked = false;

        if (userDetail != null) {
            User user = userRepository.findById(userDetail.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

            isBookmarked = bookmarkRepository.findByUserAndAnalysis(user, analysis).isPresent();
        }

        return analysisMapper.toAnalysisViewDto(analysis, isBookmarked);
    }

    @Transactional(readOnly = true)
    public List<AnalysisDto> getAnalysisByCategory(String category) {
        return analysisRepository.findAllByCategory(category).stream()
            .map(analysisMapper::toAnalysisDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnalysisDto> getAnalysisByUserId(UserDetail userDetail) {

        User user = userRepository.findById(userDetail.getId())
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return user.getUserAnalyses().stream()
            .map(UserAnalysis::getAnalysis)      // 각 UserAnalysis 객체에서 Analysis 엔티티를 추출
            .map(analysisMapper::toAnalysisDto)  // 각 Analysis 엔티티를 AnalysisDto로 변환
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAnalysisById(Long id) {
        analysisRepository.deleteById(id);
    }

    public IsAnalyzedDto isAnalyzed(String url) {
        String normalizedUrl = urlNormalizationService.normalize(url);
        Optional<MainNews> mainNewsOpt = mainNewsRepository.findFirstByUrlOrCanonicalUrl(url, normalizedUrl);

        if (mainNewsOpt.isPresent()) {
            // 뉴스가 존재할 경우
            MainNews mainNews = mainNewsOpt.get();
            String analysisPlan = mainNews.getAnalysis().getPlan();

            log.info("Analysis found for URL: {}. Plan: {}", url, analysisPlan);

            return IsAnalyzedDto.builder()
                .isAnalyzed(true)
                .plan(analysisPlan)
                .build();
        } else {
            // 뉴스가 존재하지 않을 경우
            log.info("No analysis found for URL: {}", url);

            return IsAnalyzedDto.builder()
                .isAnalyzed(false)
                .plan(null) // 분석된 플랜이 없음
                .build();
        }
    }
}