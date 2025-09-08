package com.example.nocap.domain.analysis.service;

import com.example.nocap.domain.analysis.dto.AnalysisRequestDto;
import com.example.nocap.domain.analysis.dto.CrawledResponseDto;
import com.example.nocap.domain.analysis.dto.NewsSearchRequestDto;
import com.example.nocap.domain.analysis.dto.NewsSearchResponseDto;
import com.example.nocap.domain.analysis.dto.SbertRequestDto;
import com.example.nocap.domain.analysis.dto.SbertResponseDto;
import com.example.nocap.domain.analysis.dto.TitleCategoryDto;
import com.example.nocap.domain.analysis.entity.Analysis;
import com.example.nocap.domain.analysis.mapper.AnalysisMapper;
import com.example.nocap.domain.analysis.repository.AnalysisRepository;
import com.example.nocap.domain.mainnews.entity.MainNews;
import com.example.nocap.domain.mainnews.mapper.MainNewsMapper;
import com.example.nocap.domain.mainnews.repository.MainNewsRepository;
import com.example.nocap.domain.news.mapper.NewsMapper;
import com.example.nocap.domain.user.entity.User;
import com.example.nocap.domain.user.repository.UserRepository;
import com.example.nocap.exception.CustomException;
import com.example.nocap.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AnalysisProcessService {

    private final ArticleExtractionService articleExtractionService;
    private final OpenAIService openAIService;
    private final SearchService searchService;
    private final CrawlingService crawlingService;
    private final AnalysisSaveService analysisSaveService;
    private final NewsMapper newsMapper;
    private final MainNewsMapper mainNewsMapper;
    private final AnalysisMapper analysisMapper;
    private final MainNewsRepository mainNewsRepository;
    private final UserRepository userRepository;

    // Fast API 서버 호출용 추가 의존성
    private  final WebClient webClient;

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

    public SbertResponseDto analyzeUrlAndPrepareRequest(AnalysisRequestDto analysisRequestDto) {

        String url = analysisRequestDto.getUrl();
        Long userId = analysisRequestDto.getUserId();

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        String plan = user.getRole(); // NORMAL || PREMIUM
        //String plan = "PREMIUM";

        // 1. URL에서 메인 기사 추출
        MainNews mainNews = articleExtractionService.extract(url);
        System.out.println("추출된 기사 제목 : " + mainNews.getTitle());

        Optional<MainNews> existingMainNewsOpt = mainNewsRepository.findByCanonicalUrl(mainNews.getCanonicalUrl());

        if (existingMainNewsOpt.isPresent()) {
            // 뉴스가 이미 분석된 경우
            Analysis existingAnalysis = existingMainNewsOpt.get().getAnalysis();
            // 유저는 프리미엄인데 분석하려는 뉴스의 비젼이 일반인 경우
            if ("PREMIUM".equalsIgnoreCase(plan) && "NORMAL".equalsIgnoreCase(existingAnalysis.getVersion())) {
                SbertRequestDto sbertRequestDto = SbertRequestDto.builder()
                    .plan(plan)
                    .category(existingAnalysis.getCategory())
                    .mainNewsDto(mainNewsMapper.toMainNewsDto(mainNews))
                    .newsDtos(newsMapper.toNewsDtoList(existingAnalysis.getRelatedNews()))
                    .build();
                SbertResponseDto sbertResponseDto = requestFastAPi(sbertRequestDto);
                // 비교를 갱신하여 다시 저장
                analysisSaveService.updateAnalysisData(existingAnalysis, sbertResponseDto);

            }
            // 이미 있는 분석을 반환
            return analysisMapper.toSbertResponseDto(existingAnalysis);

        }

        // 2. OpenAI로 새 제목/카테고리 생성
        TitleCategoryDto titleCategoryDto = openAIService.generate(mainNews.getContent());

        //가짜 타이틀
//        TitleCategoryDto titleCategoryDto = TitleCategoryDto.builder()
//                .title("‘이태원 참사 트라우마’ 실종 소방대원 숨진 채 발견")
//                    .category("기타")
//                        .build();
//        TitleCategoryDto titleCategoryDto = TitleCategoryDto.builder()
//                .title("21대 대선 정치 댓글 60% 이상 감소…네이버 \"악플 근절 정책 영향\"")
//                    .category("정치")
//                        .build();

        System.out.println("챗지피티가 새로 구성한 제목 : " + titleCategoryDto.getTitle());
        System.out.println("챗지피티가 찾은 카테고리 : " + titleCategoryDto.getCategory());

        // 3. 새 제목으로 관련 뉴스 검색
        NewsSearchRequestDto newsSearchRequestDto = NewsSearchRequestDto.builder()
            .rawKeyword(titleCategoryDto.getTitle())
            .excludeTitle(mainNews.getTitle())
            .excludeUrl(mainNews.getUrl())
            .build();
        NewsSearchResponseDto searchResult = searchService.searchNewsDto(newsSearchRequestDto);
        System.out.println("네이버 뉴스 API 호출 시간 : " + searchResult.getLastBuildDate());

        // 4. 검색된 뉴스들 크롤링
        CrawledResponseDto crawledResponseDto = crawlingService.crawlNews(searchResult);

        // 5. 최종 DTO 빌드
        SbertRequestDto sbertRequestDto = SbertRequestDto.builder()
            .plan(plan)
            .category(titleCategoryDto.getCategory())
            .mainNewsDto(mainNewsMapper.toMainNewsDto(mainNews))
            .newsDtos(crawledResponseDto.getNewsDtos())
            .build();

        // 6. FastAPI 호출
        SbertResponseDto sbertResponseDto = requestFastAPi(sbertRequestDto);
        assert sbertResponseDto != null;
        analysisSaveService.saveAnalysisData(analysisRequestDto.getUserId(), mainNews,
            sbertResponseDto);
        return sbertResponseDto;
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
}