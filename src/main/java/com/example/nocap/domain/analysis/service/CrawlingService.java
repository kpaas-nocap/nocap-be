package com.example.nocap.domain.analysis.service;

import com.example.nocap.domain.analysis.dto.CrawledResponseDto;
import com.example.nocap.domain.analysis.dto.NewsSearchResponseDto;
import com.example.nocap.domain.news.dto.NewsRequestDto;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CrawlingService {

    public CrawledResponseDto crawlNews(NewsSearchResponseDto newsSearchResponseDto) {
        List<NewsRequestDto> crawledNews = new ArrayList<>();

        for (NewsSearchResponseDto.Item item : newsSearchResponseDto.getItems()) {
            String url = item.getLink();
            String fullHtmlContent;

            try {
                Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .followRedirects(true)
                    .get();

                // 네이버 뉴스인지 확인하고 적절한 크롤링 수행
                if (url.contains("news.naver.com")) {
                    fullHtmlContent = extractNaverNewsContent(doc);
                } else {
                    // 일반 뉴스 사이트
                    Element bodyEl = doc.selectFirst("article");
                    if (bodyEl == null) {
                        bodyEl = doc.body();
                    }
                    fullHtmlContent = bodyEl.html();
                }

            } catch (IOException e) {
                log.error("크롤링 실패: {} - {}", url, e.getMessage());
                continue;
            }

            if (fullHtmlContent != null && !fullHtmlContent.trim().isEmpty()) {
                crawledNews.add(new NewsRequestDto(
                    // 크롤링은 NaverNew에서 가공된 버젼을 사용하되
                    // 차후 뉴스 상세페이지 및 분석 여부 판단을 위해 원본 링크로 저장
                    item.getOriginallink(), // 크롤링은 NaverNew에서 가공된 버젼을 사용하되
                    cleanTitle(item.getTitle()),
                    item.getPubDate(),
                    fullHtmlContent // HTML이 포함된 본문 전달
                ));
                log.debug("뉴스 크롤링 성공: {}", url);
            }
        }

        log.info("총 {}개 뉴스 중 {}개 크롤링 완료",
            newsSearchResponseDto.getItems().size(), crawledNews.size());

        return CrawledResponseDto.builder()
            .newsDtos(crawledNews)
            .build();
    }

    private String extractNaverNewsContent(Document doc) {
        String[] contentSelectors = {
            "#dic_area",        // 일반 텍스트 뉴스
            "#articeBody",      // 연예/스포츠 뉴스 등
            ".go_trans"         // TV 뉴스
        };

        Element contentArea = null;
        for (String selector : contentSelectors) {
            contentArea = doc.selectFirst(selector);
            if (contentArea != null) {
                break; // 가장 먼저 찾아지는 요소를 본문으로 간주
            }
        }

        if (contentArea == null) {
            log.warn("네이버 뉴스 본문 영역을 찾을 수 없음: {}", doc.location());
            return null;
        }

        // 이미지, 동영상, 광고, 기자 정보, 저작권 안내 등
        contentArea.select("script, style, .end_photo_org, .img_desc, .vod_player, .ad, " +
            ".byline, .copyright, [class*='social'], [id*='social']").remove();

        // HTML 구조를 유지한 본문 추출
        String htmlContent = contentArea.html();

        // 연속된 <br> 태그 정리 및 앞뒤 공백 제거
        return htmlContent
            .replaceAll("(<br\\s*/?\\s*>\\s*){3,}", "<br><br>") // <br>이 3개 이상 연속되면 2개로 줄임
            .trim();
    }

    private String cleanTitle(String title) {
        if (title == null) return null;

        // HTML 태그 제거 및 특수문자 정리
        return Jsoup.parse(title).text()
            .replaceAll("&quot;", "\"")
            .replaceAll("&amp;", "&")
            .replaceAll("&lt;", "<")
            .replaceAll("&gt;", ">")
            .trim();
    }
}