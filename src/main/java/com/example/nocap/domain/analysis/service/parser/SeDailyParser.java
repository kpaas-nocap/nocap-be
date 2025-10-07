package com.example.nocap.domain.analysis.service.parser;

import com.example.nocap.domain.mainnews.entity.MainNews;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Order(2) // ChosunBizParser 다음에 실행되도록 우선순위 설정
@Slf4j
public class SeDailyParser implements SiteSpecificParser {

    @Override
    public boolean canParse(String url) {
        return url.contains("sedaily.com");
    }

    @Override
    public MainNews extract(Document doc, String url) {
        log.info(">>>>> Executing SeDailyParser for: {}", url);

        // 1. 정보 추출
        String title = extractTitle(doc);
        String image = getMetaTagContent(doc, "og:image");
        String dateString = extractPublishedDate(doc);
        String plainContent = extractContent(doc);

        log.info(">>>>> JSON Title: {}", title);
        log.info(">>>>> JSON Date: {}", dateString);

        if (plainContent == null || plainContent.isBlank()) {
            log.warn("SeDailyParser: 본문 내용을 추출하지 못했습니다. URL: {}", url);
            return null;
        }

        log.info(">>>>> JSON Content: {}", plainContent);

        // 2. MainNews 객체 생성 및 반환
        return MainNews.builder()
            .url(url)
            .canonicalUrl(getMetaTagContent(doc, "og:url"))
            .title(unescape(title))
            .content(plainContent)
            .image(image)
            .date(parseDateString(dateString))
            .build();
    }


    private String extractTitle(Document doc) {
        Element titleElement = doc.selectFirst("h1.art_tit");
        if (titleElement != null) {
            return titleElement.text();
        }
        return getMetaTagContent(doc, "og:title"); // Fallback
    }

    private String extractContent(Document doc) {
        Element contentElement = doc.selectFirst("div.article_view[itemprop=articleBody]");
        if (contentElement != null) {
            // 본문 내부의 불필요한 광고, 저작권, 기자 정보 등 제거
            contentElement.select("figure, figcaption, .art_rel, .sub_ad_banner, .article_copy, .reporter_wrap").remove();

            // HTML을 문자열로 가져온 뒤, Jsoup.parse().text()로 태그를 확실하게 제거
            String contentHtml = contentElement.html();
            return Jsoup.parse(contentHtml).text();
        }
        return null;
    }

    private String extractPublishedDate(Document doc) {
        // "입력" 텍스트를 포함하는 첫 번째 span.url_txt 요소 찾기
        Element dateElement = doc.select("span.url_txt:contains(입력)").first();
        if (dateElement != null) {
            // "입력" 단어 제거 후 날짜 부분만 추출
            return dateElement.text().replace("입력", "").trim();
        }
        return getMetaTagContent(doc, "article:published_time"); // Fallback
    }

    private String getMetaTagContent(Document doc, String property) {
        Element metaTag = doc.selectFirst("meta[property=" + property + "]");
        return (metaTag != null) ? metaTag.attr("content") : "";
    }

    private String unescape(String text) {
        if (text == null) return null;
        return text.replace("\\\"", "\"").replace("\\'", "'");
    }

    private String parseDateString(String dateString) {
        if (dateString == null || dateString.isBlank()) return null;

        // "yyyy-MM-dd HH:mm:ss" 형식에 맞는 포맷터
        DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            return LocalDateTime.parse(dateString, customFormatter).format(outputFormatter);
        } catch (Exception e) {
            log.warn("SeDailyParser: 날짜 파싱 실패 ({}): {}", customFormatter, dateString);
        }

        // 다른 범용 포맷들도 시도
        List<DateTimeFormatter> formatters = List.of(
            DateTimeFormatter.ISO_OFFSET_DATE_TIME, DateTimeFormatter.RFC_1123_DATE_TIME
        );
        for (DateTimeFormatter formatter : formatters) {
            try {
                return ZonedDateTime.parse(dateString, formatter).format(outputFormatter);
            } catch (Exception ex) { /* 다음 시도 */ }
        }
        return null;
    }
}