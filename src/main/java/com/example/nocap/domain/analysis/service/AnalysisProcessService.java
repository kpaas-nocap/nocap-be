package com.example.nocap.domain.analysis.service;

import com.example.nocap.auth.dto.response.UserDetail;
import com.example.nocap.domain.analysis.dto.AnalysisRequestDto;
import com.example.nocap.domain.analysis.dto.AnalysisViewDto;
import com.example.nocap.domain.analysis.dto.CrawledResponseDto;
import com.example.nocap.domain.analysis.dto.NewsSearchRequestDto;
import com.example.nocap.domain.analysis.dto.NewsSearchResponseDto;
import com.example.nocap.domain.analysis.dto.SbertRequestDto;
import com.example.nocap.domain.analysis.dto.SbertResponseDto;
import com.example.nocap.domain.analysis.dto.TitleCategoryDto;
import com.example.nocap.domain.analysis.entity.Analysis;
import com.example.nocap.domain.analysis.mapper.AnalysisMapper;
import com.example.nocap.domain.bookmark.repository.BookmarkRepository;
import com.example.nocap.domain.mainnews.entity.MainNews;
import com.example.nocap.domain.mainnews.mapper.MainNewsMapper;
import com.example.nocap.domain.mainnews.repository.MainNewsRepository;
import com.example.nocap.domain.news.mapper.NewsMapper;
import com.example.nocap.domain.searchnews.dto.SearchNewsDto;
import com.example.nocap.domain.user.entity.User;
import com.example.nocap.domain.user.repository.UserRepository;
import com.example.nocap.exception.CustomException;
import com.example.nocap.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AnalysisProcessService {

    private final OpenAIService openAIService;
    private final NaverNewsService naverNewsService;
    private final CrawlingService crawlingService;
    private final AnalysisSaveService analysisSaveService;
    private final AnalysisUpdateService analysisUpdateService;
    private final NewsMapper newsMapper;
    private final MainNewsMapper mainNewsMapper;
    private final AnalysisMapper analysisMapper;
    private final MainNewsRepository mainNewsRepository;
    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UrlNormalizationService urlNormalizationService;

    // Fast API 서버 호출용 추가 의존성
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private static final Logger log = LoggerFactory.getLogger(AnalysisProcessService.class);

    public boolean healthCheck() {
        try {
            return Boolean.TRUE.equals(webClient.get()               // 1. GET 요청을 보냄
                .uri("/health")              // 2. FastAPI 서버의 /health 엔드포인트로
                .retrieve()                  // 3. 응답을 받음 (오류 코드는 여기서 예외 발생)
                .toBodilessEntity()          // 4. 응답 본문(body)은 필요 없으므로 무시
                .flatMap(response -> Mono.just(
                    response.getStatusCode().is2xxSuccessful())) // 5. 상태 코드가 2xx(성공)이면 true로 변환
                .onErrorReturn(false)        // 6. 3번 단계 등에서 오류 발생 시 false를 반환
                .block());                    // 7. 비동기 작업이 끝날 때까지 기다리고 최종 boolean 값 반환
        } catch (Exception e) {
            // .block() 에서 타임아웃 등의 예외가 발생할 경우를 대비한 최종 안전장치
            return false;
        }
    }

    @Transactional
    public AnalysisViewDto analyzeUrlAndPrepareRequest(AnalysisRequestDto analysisRequestDto, UserDetail userDetail)
        throws JsonProcessingException {

        // 메인뉴스를 구성하기 위해 요청된 뉴스를 객체에 담음
        SearchNewsDto requestNews = analysisRequestDto.getSearchNewsDto();
        String url = requestNews.getUrl();
        Long userId = userDetail.getId();
        String plan = analysisRequestDto.getPlan(); // NORMAL || PREMIUM

        // 토큰에서 유저를 꺼냄
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 1. MainNews 구성
        MainNews mainNews = MainNews.builder()
            .url(url)
            .canonicalUrl(urlNormalizationService.normalize(url))
            .title(requestNews.getTitle())
            .date(requestNews.getDate())
            .content(requestNews.getContent())
            .image(requestNews.getImage())
            .build();

        Optional<MainNews> existingMainNewsOpt = mainNewsRepository.findByCanonicalUrl(
            mainNews.getCanonicalUrl());
        log.info("Canonical url: " + mainNews.getCanonicalUrl());

        Analysis finalAnalysis;

        if (existingMainNewsOpt.isPresent()) {
            log.info("existing analysis...");
            MainNews existingMainNews = existingMainNewsOpt.get();
            Analysis existingAnalysis = existingMainNews.getAnalysis();

            if (plan.equals(existingAnalysis.getPlan())) {
                throw new CustomException(ErrorCode.ALREADY_ANALYZED_NEWS);
            }
            if (plan.equals("PREMIUM") && !user.getRole().equals("PREMIUM")) {
                usePoint(user);
            }
            SbertResponseDto sbertResponseDto = performExternalAnalysis(mainNews, plan);
            analysisUpdateService.updateAnalysisData(existingAnalysis, plan, sbertResponseDto);
            finalAnalysis = existingAnalysis;

        } else {
            log.info("new analysis...");
            if (plan.equals("PREMIUM") && !user.getRole().equals("PREMIUM")) {
                usePoint(user);
            }
            SbertResponseDto sbertResponseDto = performExternalAnalysis(mainNews, plan);
            finalAnalysis = analysisSaveService.saveAnalysisData(userId, plan, mainNews, sbertResponseDto);
        }

        boolean isBookmarked = bookmarkRepository.findByUserAndAnalysis(user, finalAnalysis).isPresent();

        // 5. 최종 DTO로 변환하여 반환
        return analysisMapper.toAnalysisViewDto(finalAnalysis, isBookmarked);
    }

    private SbertResponseDto performExternalAnalysis(MainNews mainNews, String plan) throws JsonProcessingException {
        // OpenAI로 키워드/카테고리 생성
        TitleCategoryDto titleCategoryDto = openAIService.generate(mainNews.getContent());
        log.info("관련 뉴스 검색 키워드: {}", titleCategoryDto.getTitle());

        // 키워드로 관련 뉴스 검색
        NewsSearchRequestDto newsSearchRequestDto = NewsSearchRequestDto.builder()
            .rawKeyword(titleCategoryDto.getTitle())
            .excludeTitle(mainNews.getTitle())
            .excludeUrl(mainNews.getUrl())
            .build();
        NewsSearchResponseDto searchResult = naverNewsService.searchNewsDto(newsSearchRequestDto);

        // 검색된 뉴스들 크롤링
        CrawledResponseDto crawledResponseDto = crawlingService.crawlNews(searchResult);

        // FastAPI 요청 DTO 빌드
        SbertRequestDto sbertRequestDto = SbertRequestDto.builder()
            .plan(plan)
            .category(titleCategoryDto.getCategory())
            .mainNewsDto(mainNewsMapper.toMainNewsRequestDto(mainNews))
            .newsDtos(crawledResponseDto.getNewsDtos())
            .build();

        log.info("Request JSON to FastAPI: {}", new ObjectMapper().writeValueAsString(sbertRequestDto));

        // FastAPI 호출
        return requestFastAPI(sbertRequestDto);
    }

    private SbertResponseDto requestFastAPI(SbertRequestDto sbertRequestDto) {
        SbertResponseDto sbertResponseDto;

        try {
            return sbertResponseDto = webClient.post() // POST 요청
                .uri("/analyze") //FastAPI 서버의 엔드포인트 경로
                .bodyValue(sbertRequestDto)
                .retrieve()
                .bodyToMono(SbertResponseDto.class)
                .block(); // 비동기 처리를 동기적으로 기다림
        } catch (Exception e) {
            log.error("FastAPI server call failed: {}", e.getMessage());
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    private void usePoint(User user) {
        int remainPoint = user.getPoint();
        if (remainPoint <= 0) {
            throw new CustomException(ErrorCode.NO_POINT);
        }
        user.setPoint(remainPoint-1);
    }
}