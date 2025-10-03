package com.example.nocap.domain.searchnews.service;

import com.example.nocap.domain.analysis.service.ArticleExtractionService;
import com.example.nocap.domain.searchnews.dto.NewsSearchResponseDto;
import com.example.nocap.domain.searchnews.dto.SearchNewsDto;
import com.example.nocap.global.NaverApiConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class SearchNewsService {

    private static final String SECTION_URL_TEMPLATE = "https://news.naver.com/section/%d";
    private final NaverApiConfig naverApiConfig;
    private final ArticleExtractionService articleExtractionService;
    private final ObjectMapper objectMapper;

    private static final Logger log = LoggerFactory.getLogger(SearchNewsService.class);



    public List<SearchNewsDto> getNewsByCategory(int category) {
        String sectionUrl = String.format(SECTION_URL_TEMPLATE, category);
        try {
            Document sectionDoc = Jsoup.connect(sectionUrl).userAgent("Mozilla/5.0").get();
            List<String> naverUrls = sectionDoc.select("a[href^=\"https://n.news.naver.com/mnews/article\"]")
                .stream()
                .map(a -> a.absUrl("href"))
                .distinct()
                .filter(u -> !u.contains("/article/comment/"))
                .limit(10)
                .toList();

            return naverUrls.parallelStream()
                .map(naverUrl -> {
                    try {
                        Document doc = Jsoup.connect(naverUrl).userAgent("Mozilla/5.0").get();
                        return parse(doc);
                    } catch (Exception e) {
                        log.error("카테고리 기사 크롤링 실패: {} - 오류: {}", naverUrl, e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "섹션 크롤링 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Document 객체를 받아 뉴스 정보를 추출하는 메소드
     */
    public SearchNewsDto parse(Document doc) {

        // 1. 원본 기사 URL 추출
        Element originalLinkElement = doc.selectFirst("a.media_end_head_origin_link");
        String finalUrl = (originalLinkElement != null) ? originalLinkElement.attr("href") : doc.location();

        // 2. 제목, 이미지, 날짜 추출
        String title = getMetaTagContent(doc, "og:title");
        String image = getMetaTagContent(doc, "og:image");
        String date = extractPublishedDate(doc);

        // ✨ 3. 본문을 HTML 태그 포함하여 추출
        Element bodyEl = doc.selectFirst("#dic_area");
        if (bodyEl == null) {
            throw new IllegalStateException("본문 요소를 찾을 수 없습니다: " + doc.location());
        }

        // ✨ 4. 본문 내부의 불필요한 요소(이미지 테이블, 기자 정보 등) 제거
        bodyEl.select("table, .byline").remove();
        String htmlContent = bodyEl.html()
            .replaceAll("(<br>\\s*){3,}", "<br><br>") // 연속된 <br> 정리
            .trim();

        return SearchNewsDto.builder()
            .url(finalUrl)
            .title(unescape(title))
            .date(parseDateString(date))
            .content(htmlContent) // HTML 태그가 포함된 본문
            .image(image)
            .build();
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

    private String extractPublishedDate(Document doc) {
        String[] dateSelectors = {"meta[property=article:published_time]", "span.num_date", "span._ARTICLE_DATE_TIME", "span.t11", "span[class*='date']", "time"};
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
        if (dateString == null || dateString.isBlank()) return null;
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<DateTimeFormatter> formatters = List.of(
            DateTimeFormatter.RFC_1123_DATE_TIME, DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy. M. d. HH:mm"), DateTimeFormatter.ofPattern("yyyy.MM.dd. a H:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
        for (DateTimeFormatter formatter : formatters) {
            try {
                if (dateString.contains("+") || dateString.contains("Z") || dateString.contains("GMT")) {
                    return ZonedDateTime.parse(dateString, formatter).format(outputFormatter);
                } else {
                    return LocalDateTime.parse(dateString, formatter).format(outputFormatter);
                }
            } catch (Exception e) { /* 다음 포맷터로 계속 */ }
        }
        Pattern pattern = Pattern.compile("\\d{4}[-.]\\d{2}[-.]\\d{2}");
        java.util.regex.Matcher matcher = pattern.matcher(dateString);
        if (matcher.find()) {
            return matcher.group(0).replace(".", "-");
        }
        return null;
    }

    public List<SearchNewsDto> getNewsByKeyword(String keyword) {
        log.info("키워드 '{}'로 뉴스 검색을 시작합니다.", keyword);
        String jsonString = requestRawJson(keyword);
        NewsSearchResponseDto responseDto = jsonToDto(jsonString);
        sanitizeItems(responseDto);

        return responseDto.getItems().parallelStream()
            .map(item -> {
                String originalUrl = item.getOriginallink();
                if (originalUrl == null || originalUrl.isEmpty()) {
                    return null;
                }
                try {
                    var extracted = articleExtractionService.extract(originalUrl);
                    return SearchNewsDto.builder()
                        .url(originalUrl)
                        .title(item.getTitle())
                        .date(convertRfcToSimpleFormat(item.getPubDate())) // 날짜 변환
                        .content(extracted.getContent())
                        .image(extracted.getImage())
                        .build();
                } catch (Exception e) {
                    log.error("개별 기사 크롤링 실패: {} - 오류: {}", originalUrl, e.getMessage());
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private String convertRfcToSimpleFormat(String rfcDateString) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.RFC_1123_DATE_TIME;
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(rfcDateString, inputFormatter);
            return zonedDateTime.format(outputFormatter);
        } catch (Exception e) {
            log.warn("날짜 형식 변환 실패: {}", rfcDateString);
            return rfcDateString; // 변환 실패 시 원본 반환
        }
    }

    private String requestRawJson(String rawKeyword) {
        String query;
        query = URLEncoder.encode(rawKeyword, StandardCharsets.UTF_8);

        String apiURL = "https://openapi.naver.com/v1/search/news.json?query=" + query + "&display=20&sort=sim";

        Map<String, String> headers = new HashMap<>();
        headers.put("X-Naver-Client-Id", naverApiConfig.getClientId());
        headers.put("X-Naver-Client-Secret", naverApiConfig.getClientSecret());

        HttpURLConnection con = connect(apiURL);
        try {
            con.setRequestMethod("GET");
            headers.forEach(con::setRequestProperty);

            int code = con.getResponseCode();
            InputStream is = (code == HttpURLConnection.HTTP_OK)
                ? con.getInputStream()
                : con.getErrorStream();
            return readBody(is);
        } catch (IOException e) {
            throw new RuntimeException("API 요청 실패", e);
        } finally {
            con.disconnect();
        }
    }

    private NewsSearchResponseDto jsonToDto(String json) {
        try {
            return objectMapper.readValue(json, NewsSearchResponseDto.class);
        } catch (IOException e) {
            throw new RuntimeException("JSON → DTO 변환 실패", e);
        }
    }

    private void sanitizeItems(NewsSearchResponseDto newsSearchResponseDto) {
        if (newsSearchResponseDto == null || newsSearchResponseDto.getItems() == null) return;
        for (NewsSearchResponseDto.Item item : newsSearchResponseDto.getItems()) {
            if (item.getTitle() != null) {
                item.setTitle(Jsoup.parse(item.getTitle()).text());
            }
        }
    }

    private HttpURLConnection connect(String apiUrl) {
        try {
            URL url = new URL(apiUrl);
            return (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw new RuntimeException("연결 실패: " + apiUrl, e);
        }
    }

    private String readBody(InputStream body) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(body))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException("응답 읽기 실패", e);
        }
    }
}