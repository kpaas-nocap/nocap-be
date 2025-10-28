package com.example.nocap.domain.popnews.service;

import com.example.nocap.domain.analysis.service.ArticleExtractionService;
import com.example.nocap.domain.popnews.dto.PopNewsDetailDto;
import com.example.nocap.domain.popnews.dto.PopNewsDto;
import com.example.nocap.domain.popnews.dto.PopNewsSummaryDto;
import com.example.nocap.domain.popnews.entity.PopNews;
import com.example.nocap.domain.popnews.mapper.PopNewsMapper;
import com.example.nocap.domain.popnews.repository.PopNewsRepository;
import com.example.nocap.exception.CustomException;
import com.example.nocap.exception.ErrorCode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PopNewsService {

    private final PopNewsRepository popNewsRepository;
    private final PopNewsMapper popNewsMapper;
    private final ArticleExtractionService articleExtractionService;


    @Transactional
    @Scheduled(cron = "0 0 * * * ?") // 정각마다
    // @Scheduled(cron = "0 * * * * ?") // 분단위 테스트용
    public void requestPopNews() {
        System.out.println("인기뉴스 크롤링이 수행됩니다.");

        // 배포시에는 살려야할 부분과 서버에 설치해야되는 것들
         //System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");
        // sudo apt-get install -y chromium-browser
        // sudo apt-get install -y chromium-chromedriver



        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // 서버 환경에서는 브라우저 창을 띄우지 않음
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://news.daum.net/");

            // '주요 뉴스' 섹션이 자바스크립트로 로딩될 때까지 최대 10초간 대기
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            String headlineSelector = "div.box_news_headline2 a.item_newsheadline2";
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(headlineSelector)));

            // 로딩이 완료된 페이지의 최종 HTML 소스를 가져옴
            String pageSource = driver.getPageSource();

            // 가져온 HTML을 Jsoup으로 파싱하여 URL 목록을 추출
            Document doc = Jsoup.parse(pageSource);
            Elements links = doc.select(headlineSelector);
            List<String> headlineUrls = links.stream()
                .map(link -> link.attr("href"))
                .distinct()
                .limit(10)
                .collect(Collectors.toList());

            List<PopNewsDto> finalPopNewsList = new ArrayList<>();

            // 각 URL을 순회하며 상세 정보를 크롤링
            for (String url : headlineUrls) {
                try {
                    var extractedArticle = articleExtractionService.extract(url);

                    if (extractedArticle != null && extractedArticle.getContent() != null && !extractedArticle.getContent().isBlank()) {
                        finalPopNewsList.add(PopNewsDto.builder()
                            .url(extractedArticle.getUrl())
                            .title(extractedArticle.getTitle())
                            .content(extractedArticle.getContent())
                            .image(extractedArticle.getImage())
                                .date(extractedArticle.getDate())
                            .build());
                    }
                } catch (Exception e) {
                    throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
                }
            }
            if (!finalPopNewsList.isEmpty()) {
                List<PopNews> popNewsEntities = popNewsMapper.toPopNewsEntityList(finalPopNewsList);
                popNewsRepository.deleteAllInBatch();
                popNewsRepository.saveAll(popNewsEntities);
            }

        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            // 작업이 끝나면 반드시 브라우저 프로세스를 종료하여 메모리 누수를 방지
            driver.quit();
        }
    }

    public List<PopNewsSummaryDto> getAllPopNews() {
        return popNewsMapper.toPopNewsSummaryDtoList(popNewsRepository.findAll());
    }

    public PopNewsDetailDto getPopNews(Long popNewsId) {
        PopNews popNews = popNewsRepository.findById(popNewsId)
            .orElseThrow(() -> new CustomException(ErrorCode.POP_NEWS_NOT_FOUND));
        return popNewsMapper.toPopNewsDetailDto(popNews);
    }
}
