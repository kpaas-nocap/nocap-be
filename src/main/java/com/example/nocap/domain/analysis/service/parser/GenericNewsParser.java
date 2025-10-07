package com.example.nocap.domain.analysis.service.parser;

import com.example.nocap.domain.mainnews.entity.MainNews;
import com.example.nocap.exception.CustomException;
import com.example.nocap.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import net.dankito.readability4j.Article;
import net.dankito.readability4j.Readability4J;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Order(Integer.MAX_VALUE) // 가장 마지막에 실행되도록 우선순위를 낮게 설정
@Slf4j // @Slf4j 추가
public class GenericNewsParser implements SiteSpecificParser {

    @Override
    public boolean canParse(String url) {
        // 모든 URL을 처리할 수 있는 최종 Fallback 파서이므로 항상 true를 반환
        return true;
    }

    @Override
    public MainNews extract(Document doc, String url) {
        log.info("   >>>>> Executing GenericNewsParser for: {}", url);

        if (!isArticleByConfidenceScore(doc)) {
            throw new CustomException(ErrorCode.NOT_A_NEWS_ARTICLE);
        }

        preprocessDocument(doc);

        String ogTitle = getMetaTagContent(doc, "og:title");
        String ogImage = getMetaTagContent(doc, "og:image");

        Article article = new Readability4J(url, doc.html()).parse();
        String rawContent = article.getTextContent();

        String finalTitle = (!ogTitle.isEmpty()) ? ogTitle : article.getTitle();
        finalTitle = unescape(finalTitle);
        log.info(">>>>> JSON Title: {}", finalTitle);

        String cleanedContent = postProcessContent(rawContent, finalTitle);

        String pubDateString = extractPublishedDate(doc);
        String pubDate = parseDateString(pubDateString);
        log.info(">>>>> JSON Date: {}", pubDate);

        String canonicalUrl = createCanonicalUrl(doc, url);

        if (cleanedContent == null || cleanedContent.trim().isEmpty()) {
            throw new CustomException(ErrorCode.NEWS_EXTRACTING_ERROR);
        }

        log.info(">>>>> JSON Content: {}", cleanedContent);


        return MainNews.builder()
            .url(url)
            .canonicalUrl(canonicalUrl)
            .title(finalTitle)
            .content(cleanedContent)
            .image(ogImage)
            .date(pubDate)
            .build();
    }
    private String postProcessContent(String text, String title) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String processedText = text;

        processedText = processedText.replace("\\\"", "\"").replace("\\'", "'");
        processedText = processedText.replace("\\", "");

        if (title != null && !title.isBlank()) {
            // 비교를 위해 양쪽에서 모든 따옴표(작은/큰)를 제거
            String normalizedTitle = title.replace("'", "").replace("\"", "");
            String normalizedContent = processedText.replace("'", "").replace("\"", "");

            if (normalizedContent.startsWith(normalizedTitle)) {
                processedText = processedText.substring(title.length()).trim();
            }
        }

        processedText = processedText.replaceAll("^(입력|수정).*?기자", "").trim();

        processedText = processedText.replaceAll("< 저작권자 ⓒ .*?>", "").trim();
        processedText = processedText.replaceAll("※ 본 사이트에 게재되는 정보는.*", "").trim();

        processedText = processedText.replaceAll("\\s+", " ").trim();

        return processedText;
    }

    private String createCanonicalUrl(Document doc, String originalUrl) {
        String ogUrl = getMetaTagContent(doc, "og:url");
        if (!ogUrl.isEmpty()) {
            return ogUrl;
        }
        Element canonicalLink = doc.selectFirst("link[rel=canonical]");
        if (canonicalLink != null && canonicalLink.hasAttr("href")) {
            return canonicalLink.attr("href");
        }
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
        String[] dateSelectors = {
            "meta[property=article:published_time]", "span.num_date",
            "span._ARTICLE_DATE_TIME", "span.t11", "span[class*='date']", "time"
        };
        for (String selector : dateSelectors) {
            Element dateElement = doc.selectFirst(selector);
            if (dateElement != null) {
                if (dateElement.hasAttr("content")) return dateElement.attr("content");
                if (dateElement.hasAttr("data-date-time")) return dateElement.attr("data-date-time");
                return dateElement.text();
            }
        }
        return null;
    }

    private String parseDateString(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<DateTimeFormatter> formatters = List.of(
            DateTimeFormatter.ISO_OFFSET_DATE_TIME, DateTimeFormatter.RFC_1123_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy. M. d. HH:mm"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd. a H:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
        for (DateTimeFormatter formatter : formatters) {
            try {
                return ZonedDateTime.parse(dateString, formatter).format(outputFormatter);
            } catch (java.time.format.DateTimeParseException e1) {
                try {
                    return LocalDateTime.parse(dateString, formatter).format(outputFormatter);
                } catch (java.time.format.DateTimeParseException e2) {
                    // 다음 포맷터로 계속
                }
            }
        }
        Pattern pattern = Pattern.compile("\\d{4}[-.]\\d{2}[-.]\\d{2}");
        java.util.regex.Matcher matcher = pattern.matcher(dateString);
        if (matcher.find()) {
            return matcher.group(0).replace(".", "-");
        }
        return null;
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
            List<String> essentialParams = List.of("article_id", "id", "no", "idx", "idxno", "pno", "newsid", "materialId");
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