package com.example.nocap.domain.analysis.service;

import com.example.nocap.domain.analysis.dto.AnalysisRequestDto;
import com.example.nocap.domain.analysis.dto.CrawledResponseDto;
import com.example.nocap.domain.analysis.dto.NewsSearchRequestDto;
import com.example.nocap.domain.analysis.dto.NewsSearchResponseDto;
import com.example.nocap.domain.analysis.dto.SbertRequestDto;
import com.example.nocap.domain.analysis.dto.SbertResponseDto;
import com.example.nocap.domain.analysis.dto.TitleCategoryDto;
import com.example.nocap.domain.mainnews.entity.MainNews;
import com.example.nocap.domain.mainnews.mapper.MainNewsMapper;
import com.example.nocap.exception.CustomException;
import com.example.nocap.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.lambda.model.LambdaException;

@Service
@RequiredArgsConstructor
public class AnalysisProcessService {

    private final ArticleExtractionService articleExtractionService;
    private final OpenAIService openAIService;
    private final SearchService searchService;
    private final CrawlingService crawlingService;
    private final AnalysisSaveService analysisSaveService;
    private final MainNewsMapper mainNewsMapper;

    // 람다 호출용 추가 의존성
    private final LambdaClient lambdaClient;
    private final ObjectMapper objectMapper;

    private static final Logger log = LoggerFactory.getLogger(AnalysisProcessService.class);

    @Value("${aws.lambda.function-name}")
    private String lambdaFunctionName;

    public SbertResponseDto analyzeUrlAndPrepareRequest(AnalysisRequestDto analysisRequestDto) {

        String url = analysisRequestDto.getUrl();
        String plan = analysisRequestDto.getPlan();
        Long userId = analysisRequestDto.getUserId();

        // 1. URL에서 메인 기사 추출
        MainNews mainNews = articleExtractionService.extract(url);
        System.out.println("추출된 기사 제목 : " + mainNews.getTitle());

        // 2. OpenAI로 새 제목/카테고리 생성
        TitleCategoryDto titleCategoryDto = openAIService.generate(mainNews.getContent());

//        //가짜 타이틀
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

        // 람다에 보낼 요청 DTO 로깅
//        try {
//            String jsonRequestForLogging = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sbertRequestDto);
//
//            // info 레벨로 로그 출력
//            log.info("--- Sending Payload to Lambda ---\n{}", jsonRequestForLogging);
//
//        } catch (JsonProcessingException e) {
//            // 로깅 중 오류가 발생하더라도 전체 프로세스가 멈추지 않도록 예외 처리
//            log.error("Failed to serialize SbertRequestDto to JSON for logging", e);
//        }

        // 6. 람다 호출
        SbertResponseDto sbertResponseDto;
        try {
            // DTO 객체를 JSON 문자열로 변환하고, 다시 Byte 형태로 변환
            String requestPayload = objectMapper.writeValueAsString(sbertRequestDto);
            SdkBytes payload = SdkBytes.fromUtf8String(requestPayload);

            // 람다를 호출하기 위한 요청 객체 생성
            InvokeRequest request = InvokeRequest.builder()
                .functionName(lambdaFunctionName)
                .payload(payload)
                .build();

            // 람다 함수를 동기적으로 호출
            InvokeResponse response = lambdaClient.invoke(request);

            // 람다 함수 내부에서 오류가 발생했는지 확인
            if (response.functionError() != null) {
                throw new CustomException(ErrorCode.LAMBDA_REQUEST_ERROR); // 새로운 에러 코드 정의 필요
            }

            // 람다의 응답(Byte)을 DTO 객체로 변환하여 반환
            // 1. 람다의 전체 응답(바깥쪽 JSON)을 문자열로 받음
            String outerPayload = response.payload().asUtf8String();

            // 2. 바깥쪽 JSON을 JsonNode 트리 구조로 변환
            JsonNode rootNode = objectMapper.readTree(outerPayload);

            // 3. 'body' 키를 찾아 그 안에 있는 값을 "문자열"로 추출
            String innerPayload = rootNode.get("body").asText();

            // 4. 추출한 "내부 JSON 문자열"을 우리가 원하는 최종 DTO로 변환
            sbertResponseDto = objectMapper.readValue(innerPayload, SbertResponseDto.class);

            log.info("Deserialization successful. Category: {}", sbertResponseDto.getCategory());


        } catch (JsonProcessingException e) {
            log.error("JSON processing failed: {}", e.getMessage());
            throw new CustomException(ErrorCode.JSON_PROCESSING_ERROR);
        } catch (LambdaException e) {
            log.error("AWS Lambda service error: {}", e.awsErrorDetails().errorMessage());
            throw new CustomException(ErrorCode.LAMBDA_INVOCATION_ERROR);
        } catch (Exception e) {
            log.error("An unexpected error occurred during Lambda invocation", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        analysisSaveService.saveAnalysisData(analysisRequestDto.getUserId(), mainNews, sbertResponseDto);
        return sbertResponseDto;
    }
}