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
    private final NewsMapper newsMapper;
    private final MainNewsMapper mainNewsMapper;
    private final AnalysisMapper analysisMapper;
    private final MainNewsRepository mainNewsRepository;
    private final UserRepository userRepository;
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


        // DB에서 canonicalUrl로 기존 MainNews를 찾아옴
        // 일반분석된 뉴스를 일반분석 시도하는 경우와
        // 프리미엄 분석된 뉴스를 프리미엄 분석 시도하는 경우
        Optional<MainNews> existingMainNewsOpt = mainNewsRepository.findByCanonicalUrl(mainNews.getCanonicalUrl());
        if (existingMainNewsOpt.isPresent() &&
            plan.equals(existingMainNewsOpt.get().getAnalysis().getPlan())) {

            throw new CustomException(ErrorCode.ALREADY_ANALYZED_NEWS);
        }

        // 일반유저가 프리미엄 분석을 요청하면 포인트 차감
        if (plan.equals("PREMIUM") && !user.getRole().equals("PREMIUM")) {
            usePoint(user);
        }

        // 2. OpenAI로 새 제목/카테고리 생성
        TitleCategoryDto titleCategoryDto = openAIService.generate(mainNews.getContent());

        log.info("관련 뉴스 검색 키워드: {}", titleCategoryDto.getTitle());
        log.info("분석 카테고리: {}", titleCategoryDto.getCategory());

        // 3. 새 제목으로 관련 뉴스 검색
        NewsSearchRequestDto newsSearchRequestDto = NewsSearchRequestDto.builder()
            .rawKeyword(titleCategoryDto.getTitle())
            .excludeTitle(mainNews.getTitle())
            .excludeUrl(mainNews.getUrl())
            .build();
        NewsSearchResponseDto searchResult = naverNewsService.searchNewsDto(newsSearchRequestDto);
        log.info("네이버 뉴스 API 호출 시간: {}", searchResult.getLastBuildDate());

        // 4. 검색된 뉴스들 크롤링
        CrawledResponseDto crawledResponseDto = crawlingService.crawlNews(searchResult);

        // 5. 최종 DTO 빌드
        SbertRequestDto sbertRequestDto = SbertRequestDto.builder()
            .plan(plan)
            .category(titleCategoryDto.getCategory())
            .mainNewsDto(mainNewsMapper.toMainNewsRequestDto(mainNews))
            .newsDtos(crawledResponseDto.getNewsDtos())
            .build();

        ObjectMapper mapper = new ObjectMapper();
        log.info("Request JSON: {}", mapper.writeValueAsString(sbertRequestDto));

        // 6. FastAPI 호출
        SbertResponseDto sbertResponseDto = requestFastAPi(sbertRequestDto);
        assert sbertResponseDto != null;
        Analysis analysis = analysisSaveService.saveAnalysisData(userId, plan, mainNews,
            sbertResponseDto);

        return analysisMapper.toAnalysisViewDto(analysis, false);
    }

    private SbertResponseDto requestFastAPi(SbertRequestDto sbertRequestDto) {
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