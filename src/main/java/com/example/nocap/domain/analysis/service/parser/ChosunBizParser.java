package com.example.nocap.domain.analysis.service.parser;

import com.example.nocap.domain.mainnews.entity.MainNews;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Order(1)
@Slf4j
public class ChosunBizParser implements SiteSpecificParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean canParse(String url) {
        return url.contains("biz.chosun.com");
    }

    @Override
    public MainNews extract(Document doc, String url) {
        log.info(">>>>> Executing ChosunBizParser for: {}", url);

        try {
            // 1. <script id="fusion-metadata"> 태그 찾기
            Element scriptElement = doc.selectFirst("script#fusion-metadata");
            if (scriptElement == null) {
                log.warn("ChosunBizParser [FAIL]: Fusion metadata script tag ('script#fusion-metadata') not found.");
                return null;
            }
            log.info("ChosunBizParser [SUCCESS]: Found fusion metadata script tag.");

            // 2. 스크립트 내용에서 JSON 부분만 추출
            String scriptData = scriptElement.html();
            int jsonStart = scriptData.indexOf("Fusion.globalContent=");
            if (jsonStart == -1) {
                log.warn("ChosunBizParser [FAIL]: 'Fusion.globalContent=' string not found in script.");
                return null;
            }
            // 시작 위치 조정
            jsonStart += "Fusion.globalContent=".length();
            // 끝 위치 찾기
            int jsonEnd = scriptData.indexOf("};", jsonStart);
            if (jsonEnd == -1) {
                log.warn("ChosunBizParser [FAIL]: Could not find end of JSON object ('}};').");
                return null;
            }

            String jsonString = scriptData.substring(jsonStart, jsonEnd + 1);
            JsonNode globalContent = objectMapper.readTree(jsonString);

            // 3. JSON에서 정보 추출
            String title = globalContent.path("headlines").path("basic").asText("제목 없음");
            String dateString = globalContent.path("created_date").asText(null);
            String imageUrl = getMetaTagContent(doc, "og:image");

            log.info(">>>>> JSON Title: {}", title);
            log.info(">>>>> JSON Date: {}", dateString);

            // 4. content_elements 배열에서 type이 'text'인 것들의 content만 합치기
            List<String> contentParts = new ArrayList<>();
            JsonNode contentElements = globalContent.path("content_elements");
            if (contentElements.isArray()) {
                for (JsonNode element : contentElements) {
                    if ("text".equals(element.path("type").asText())) {
                        contentParts.add(element.path("content").asText());
                    }
                }
            }
            String contentWithTags = contentParts.stream()
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "));

            String plainContent = Jsoup.parse(contentWithTags).text();

            log.info(">>>>> Content Parts Found: {}, Total Length: {}", contentParts.size(), plainContent.length());

            if (plainContent.isBlank()) {
                log.warn("ChosunBizParser: 본문 내용을 추출하지 못했습니다. URL: {}", url);
                return null;
            }
            log.info(">>>>> JSON Content: {}", plainContent);
            return MainNews.builder()
                .url(url)
                .canonicalUrl(url)
                .title(unescape(title))
                .content(plainContent)
                .image(imageUrl)
                .date(parseDateString(dateString))
                .build();

        } catch (Exception e) {
            log.error("ChosunBizParser failed to parse JSON metadata", e);
            return null;
        }
    }

    // --- 헬퍼 메소드들 ---
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
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            return ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_ZONED_DATE_TIME).format(outputFormatter);
        } catch (Exception e) {
            log.warn("ChosunBizParser: 날짜 파싱 실패 - {}", dateString);
        }
        return null;
    }
}