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
@Order(2) // 다른 전문 파서들과 실행 순서를 정하기 위함
@Slf4j
public class ChosunParser implements SiteSpecificParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean canParse(String url) {
        // biz.chosun.com이 아닌 chosun.com을 처리하도록 수정
        return url.contains("www.chosun.com") && !url.contains("biz.chosun.com");
    }

    @Override
    public MainNews extract(Document doc, String url) {
        log.info(">>>>> Executing ChosunParser for: {}", url);

        try {
            // 1. <script id="fusion-metadata"> 태그 찾기
            Element scriptElement = doc.selectFirst("script#fusion-metadata");
            if (scriptElement == null) {
                log.warn("ChosunParser [FAIL]: Fusion metadata script tag ('script#fusion-metadata') not found.");
                return null;
            }

            // 2. 스크립트 내용에서 JSON 부분만 추출
            String scriptData = scriptElement.html();
            int jsonStart = scriptData.indexOf("Fusion.globalContent=");
            if (jsonStart == -1) {
                log.warn("ChosunParser [FAIL]: 'Fusion.globalContent=' string not found in script.");
                return null;
            }
            jsonStart += "Fusion.globalContent=".length();
            int jsonEnd = scriptData.indexOf("};", jsonStart);
            if (jsonEnd == -1) {
                log.warn("ChosunParser [FAIL]: Could not find end of JSON object ('}};').");
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

            // 4. content_elements 배열에서 type이 'text' 또는 'raw_html'인 것들의 content만 합치기
            List<String> contentParts = new ArrayList<>();
            JsonNode contentElements = globalContent.path("content_elements");
            if (contentElements.isArray()) {
                for (JsonNode element : contentElements) {
                    String type = element.path("type").asText();
                    if ("text".equals(type) || "raw_html".equals(type)) {
                        String content = element.path("content").asText();
                        // raw_html의 경우, 내부의 HTML 태그를 제거
                        if ("raw_html".equals(type)) {
                            content = Jsoup.parse(content).text();
                        }
                        contentParts.add(content);
                    }
                }
            }
            String plainContent = contentParts.stream()
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "));

            if (plainContent.isBlank()) {
                log.warn("ChosunParser: 본문 내용을 추출하지 못했습니다. URL: {}", url);
                return null;
            }

            log.info(">>>>> JSON Content: {}", plainContent);

            return MainNews.builder()
                .url(url)
                .canonicalUrl(getMetaTagContent(doc, "og:url"))
                .title(unescape(title))
                .content(plainContent)
                .image(imageUrl)
                .date(parseDateString(dateString))
                .build();

        } catch (Exception e) {
            log.error("ChosunParser failed to parse JSON metadata", e);
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
            log.warn("ChosunParser: 날짜 파싱 실패 - {}", dateString);
        }
        return null;
    }
}