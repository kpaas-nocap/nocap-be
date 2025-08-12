package com.example.nocap.domain.analysis.service;

import com.example.nocap.domain.analysis.dto.*;
import com.example.nocap.domain.mainnews.entity.MainNews;
import com.example.nocap.domain.mainnews.mapper.MainNewsMapper;
import org.springframework.stereotype.Service;

@Service
public class AnalysisProcessService {

    private final ArticleExtractionService articleExtractionService;
    private final OpenAIService openAIService;
    private final SearchService searchService;
    private final CrawlingService crawlingService;
    private final MainNewsMapper mainNewsMapper;

    public AnalysisProcessService(ArticleExtractionService articleExtractionService,
        OpenAIService openAIService, SearchService searchService,
        CrawlingService crawlingService, MainNewsMapper mainNewsMapper) {
        this.articleExtractionService = articleExtractionService;
        this.openAIService = openAIService;
        this.searchService = searchService;
        this.crawlingService = crawlingService;
        this.mainNewsMapper = mainNewsMapper;
    }
    public SbertRequestDto analyzeUrlAndPrepareRequest(String url) {
        // 1. URL에서 메인 기사 추출
        MainNews mainNews = articleExtractionService.extract(url);
        System.out.println("추출된 기사 제목 : " + mainNews.getTitle());

        // 2. OpenAI로 새 제목/카테고리 생성
        TitleCategoryDto titleCategoryDto = openAIService.generate(mainNews.getContent());
        System.out.println("챗지피티가 새로 구성한 제목 : " + titleCategoryDto.getTitle());

        // 3. 새 제목으로 관련 뉴스 검색
        NewsSearchRequestDto newsSearchRequestDto = NewsSearchRequestDto.builder()
            .rawKeyword(titleCategoryDto.getTitle())
            .excludeTitle(mainNews.getTitle())
            .build();
        NewsSearchResponseDto searchResult = searchService.searchNewsDto(newsSearchRequestDto);
        System.out.println("네이버 뉴스 API 호출 시간 : " + searchResult.getLastBuildDate());

        // 4. 검색된 뉴스들 크롤링
        CrawledResponseDto crawledResponseDto = crawlingService.crawlNews(searchResult);

        // 5. 최종 DTO 빌드
        return SbertRequestDto.builder()
            .category(titleCategoryDto.getCategory())
            .mainNewsDto(mainNewsMapper.toMainNewsDto(mainNews))
            .newsDtoList(crawledResponseDto.getNewsDtoList())
            .build();
    }
}