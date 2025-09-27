package com.example.nocap.domain.analysis.service;

import com.example.nocap.domain.analysis.dto.CrawledResponseDto;
import com.example.nocap.domain.analysis.dto.NewsSearchResponseDto;
import com.example.nocap.domain.news.dto.NewsRequestDto;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

@Service
public class CrawlingService {

     //NewsResponse 에 담긴 각 아이템의 originallink 로 크롤링을 수행하여
     //CrawledNewsResponse 로 반환
    public CrawledResponseDto crawlNews(NewsSearchResponseDto newsSearchResponseDto) {
        List<NewsRequestDto> crawledNews = new ArrayList<>();

        for (NewsSearchResponseDto.Item item : newsSearchResponseDto.getItems()) {
            String url = item.getOriginallink();
            String fullHtmlContent;
            try {
                Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(5000)
                    .get();
                Element bodyEl = doc.selectFirst("article");
                if (bodyEl == null) {
                    bodyEl = doc.body();
                }
                fullHtmlContent = bodyEl.html();
            } catch (IOException e) {
                continue;
            }
            if (fullHtmlContent != null && !fullHtmlContent.trim().isEmpty()) {
                crawledNews.add(new NewsRequestDto(
                    url,
                    item.getTitle(),
                    item.getPubDate(),
                    fullHtmlContent // HTML이 포함된 본문 전달
                ));
            }
        }

        return CrawledResponseDto.builder()
            .newsDtos(crawledNews) // 빌더를 통해 값을 설정
            .build();                 // build() 메소드로 객체 생성
    }
}