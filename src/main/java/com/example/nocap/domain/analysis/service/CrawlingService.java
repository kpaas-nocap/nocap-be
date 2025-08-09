package com.example.nocap.domain.analysis.service;

import com.example.nocap.domain.analysis.dto.CrawledResponseDto;
import com.example.nocap.domain.analysis.dto.NewsSearchResponseDto;
import com.example.nocap.domain.news.dto.NewsDto;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
public class CrawlingService {

    /**
     * NewsResponse 에 담긴 각 아이템의 originallink 로 크롤링을 수행하여
     * CrawledNewsResponse 로 반환
     */
    public CrawledResponseDto crawlNews(NewsSearchResponseDto newsSearchResponseDto) {
        List<NewsDto> crawledNews = new ArrayList<>();

        for (NewsSearchResponseDto.Item item : newsSearchResponseDto.getItems()) {
            String url = item.getOriginallink();
            String fullText;
            try {
                Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(5000)
                    .get();
                if (doc.selectFirst("article") != null) {
                    fullText = doc.select("article").text();
                } else {
                    fullText = doc.body().text();
                }
            } catch (IOException e) {
                fullText = "[크롤링 실패] " + e.getMessage();
            }

            crawledNews.add(new NewsDto(
                url,
                item.getTitle(),
                fullText
            ));
        }

        return CrawledResponseDto.builder()
            .newsDtoList(crawledNews) // 빌더를 통해 값을 설정
            .build();                 // build() 메소드로 객체 생성
    }
}