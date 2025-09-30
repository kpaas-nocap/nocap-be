package com.example.nocap.domain.analysis.service;

import com.example.nocap.domain.mainnews.entity.MainNews;
import com.example.nocap.exception.CustomException;
import com.example.nocap.exception.ErrorCode;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.dankito.readability4j.Article;
import net.dankito.readability4j.Readability4J;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

@Service
public class ArticleExtractionService {

    public MainNews extract(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            if (!isArticleByConfidenceScore(doc)) {
                throw new CustomException(ErrorCode.NOT_A_NEWS_ARTICLE);
            }

            preprocessDocument(doc);

            String ogTitle = getMetaTagContent(doc, "og:title");
            String ogImage = getMetaTagContent(doc, "og:image");

            Article article = new Readability4J(url, doc.html()).parse();
            String htmlContent = article.getContent();

            String finalTitle = (!ogTitle.isEmpty()) ? ogTitle : article.getTitle();
            finalTitle = unescape(finalTitle);

            String pubDateString = extractPublishedDate(doc);
            String pubDate = parseDateString(pubDateString); // String -> LocalDateTime 변환

            String canonicalUrl = createCanonicalUrl(doc, url); // 정규화 로직 적용

            if (htmlContent == null || htmlContent.trim().isEmpty()) {
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
            }

            return MainNews.builder()
                .url(url)
                .canonicalUrl(canonicalUrl)
                .title(finalTitle)
                .content(htmlContent)
                .image(ogImage)
                .date(pubDate)
                .build();

        } catch (IOException e) {
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    private String createCanonicalUrl(Document doc, String originalUrl) {
        // 1순위: og:url
        String ogUrl = getMetaTagContent(doc, "og:url");
        if (!ogUrl.isEmpty()) {
            return ogUrl;
        }
        // 2순위: link[rel=canonical]
        Element canonicalLink = doc.selectFirst("link[rel=canonical]");
        if (canonicalLink != null && canonicalLink.hasAttr("href")) {
            return canonicalLink.attr("href");
        }
        // 3순위: normalizeUrl
        return normalizeUrl(originalUrl);
    }

    private void preprocessDocument(Document doc) {
        doc.select(".read_cont, em.img_desc, span.img_desc, .desc_thumb").remove();
    }

    private String unescape(String text) {
        if (text == null) return null;
        return text.replace("\\\"", "\"").replace("\\'", "'");
    }

    private String getMetaTagContent(Document doc, String property) {
        Element metaTag = doc.selectFirst("meta[property=" + property + "]");
        return (metaTag != null) ? metaTag.attr("content") : "";
    }

    private String extractPublishedDate(Document doc) {
        // 우선순위대로 날짜를 포함할 가능성이 높은 선택자 목록
        String[] dateSelectors = {
            "meta[property=article:published_time]", // 1순위: 국제 표준 메타 태그
            "span.num_date",
            "span._ARTICLE_DATE_TIME",              // 2순위: 네이버 뉴스
            "span.t11",                             // 3순위: 네이버 뉴스 (구버전)
            "span[class*='date']",                  // 4순위: 'date' 클래스를 포함하는 span
            "time"                                  // 5순위: <time> 태그
        };

        for (String selector : dateSelectors) {
            Element dateElement = doc.selectFirst(selector);
            if (dateElement != null) {
                if (dateElement.hasAttr("content")) return dateElement.attr("content");
                if (dateElement.hasAttr("data-date-time")) return dateElement.attr("data-date-time");
                return dateElement.text();
            }
        }
        return null; // 날짜를 찾지 못한 경우
    }

    private String parseDateString(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }

        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // 최종 출력 형식

        List<DateTimeFormatter> formatters = List.of(
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.RFC_1123_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy. M. d. HH:mm"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd. a H:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                // 시간대 정보가 포함된 형식(RFC, ISO)은 ZonedDateTime으로 파싱
                return ZonedDateTime.parse(dateString, formatter).format(outputFormatter);
            } catch (java.time.format.DateTimeParseException e1) {
                try {
                    // 시간대 정보가 없는 형식은 LocalDateTime으로 파싱
                    return LocalDateTime.parse(dateString, formatter).format(outputFormatter);
                } catch (java.time.format.DateTimeParseException e2) {
                    // 둘 다 실패하면 다음 포맷터로 계속
                }
            }
        }

        // 정규식으로 'YYYY-MM-DD' 또는 'YYYY.MM.DD' 형태만 추출 시도
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d{4}[-.]\\d{2}[-.]\\d{2}");
        java.util.regex.Matcher matcher = pattern.matcher(dateString);
        if (matcher.find()) {
            return matcher.group(0).replace(".", "-");
        }

        return null; // 모든 형식에 실패하면 null 반환
    }

    private boolean isArticleByConfidenceScore(Document doc) {
        int confidenceScore = 0;
        final int THRESHOLD = 4;

        Element ogTypeElement = doc.selectFirst("meta[property=og:type]");
        if (ogTypeElement != null && "article".equalsIgnoreCase(ogTypeElement.attr("content"))) {
            confidenceScore += 5;
        }
        if (doc.selectFirst("article") != null) {
            confidenceScore += 3;
        }
        if (doc.selectFirst("div[class*='article'], div[class*='content'], div[id*='article'], div[id*='content']") != null) {
            confidenceScore += 2;
        }
        if (doc.selectFirst("h1") != null) {
            confidenceScore += 1;
        }

        return confidenceScore >= THRESHOLD;
    }

    private String normalizeUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            String path = url.getPath();
            String query = url.getQuery();
            if (query == null || query.isEmpty()) {
                return url.getProtocol() + "://" + url.getHost() + path;
            }
            List<String> essentialParams = List.of("article_id", "id", "no", "idx", "pno", "newsid");
            String preservedQuery = Arrays.stream(query.split("&"))
                .filter(param -> essentialParams.stream().anyMatch(key -> param.startsWith(key + "=")))
                .sorted()
                .collect(Collectors.joining("&"));
            String baseUrl = url.getProtocol() + "://" + url.getHost() + path;
            return preservedQuery.isEmpty() ? baseUrl : baseUrl + "?" + preservedQuery;
        } catch (MalformedURLException e) {
            return urlString;
        }
    }
}