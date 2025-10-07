package com.example.nocap.domain.searchnews.service;

import com.example.nocap.domain.analysis.service.ArticleExtractionService;
import com.example.nocap.domain.mainnews.entity.MainNews;
import com.example.nocap.domain.searchnews.dto.NewsSearchResponseDto;
import com.example.nocap.domain.searchnews.dto.SearchNewsDto;
import com.example.nocap.exception.CustomException;
import com.example.nocap.exception.ErrorCode;
import com.example.nocap.global.NaverApiConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchNewsService {

    private final NaverApiConfig naverApiConfig;
    private final ObjectMapper objectMapper;
    private final ArticleExtractionService articleExtractionService;

    public List<SearchNewsDto> getNewsBySearch(String search) {
        if (isUrl(search)) {
            return getNewsByUrl(search);
        } else {
            return getNewsByKeyword(search);
        }
    }

    public List<SearchNewsDto> getNewsByUrl(String url) {
        MainNews mainNews = articleExtractionService.extract(url);

        if (mainNews == null) {
            throw new CustomException(ErrorCode.NEWS_EXTRACTING_ERROR);
        }

        SearchNewsDto newsDto = SearchNewsDto.builder()
            .url(mainNews.getCanonicalUrl())
            .title(mainNews.getTitle())
            .content(mainNews.getContent())
            .date(mainNews.getDate())
            .image(mainNews.getImage())
            .build();
        return List.of(newsDto);
    }

    public List<SearchNewsDto> getNewsByCategory(int category) {
        String sectionUrl = String.format("https://news.naver.com/section/%d", category);
        try {
            Document sectionDoc = Jsoup.connect(sectionUrl).userAgent("Mozilla/5.0").get();
            List<String> naverUrls = sectionDoc.select("a[href^=\"https://n.news.naver.com/mnews/article\"]")
                .stream()
                .map(a -> a.absUrl("href"))
                .distinct()
                .filter(u -> !u.contains("/article/comment/"))
                .limit(20)
                .toList();

            return naverUrls.parallelStream()
                .map(naverUrl -> {
                    try {
                        return parseNaverNews(naverUrl);
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

    public List<SearchNewsDto> getNewsByKeyword(String keyword) {
        log.info("키워드 '{}'로 뉴스 검색을 시작합니다.", keyword);
        String jsonString = requestRawJson(keyword);
        NewsSearchResponseDto responseDto = jsonToDto(jsonString);
        sanitizeItems(responseDto);

        return responseDto.getItems().parallelStream()
            .map(item -> {
                log.info("Processing Item -> Naver Link: [{}], Original Link: [{}]", item.getLink(), item.getOriginallink());

                String naverUrl = item.getLink();
                String originalUrl = item.getOriginallink();
                try {
                    SearchNewsDto parsedDto = parseNaverNews(naverUrl);
                    if (parsedDto != null) {
                        parsedDto.setUrl(originalUrl);
                        parsedDto.setTitle(item.getTitle());
                        parsedDto.setDate(parseDateString(item.getPubDate()));
                    }
                    return parsedDto;
                } catch (Exception e) {
                    log.error("키워드 기사 크롤링 실패: {} - 오류: {}", naverUrl, e.getMessage());
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private SearchNewsDto parseNaverNews(String url) throws IOException {
        Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").get();
        Element originalLinkElement = doc.selectFirst("a.media_end_head_origin_link");
        String finalUrl = (originalLinkElement != null) ? originalLinkElement.attr("href") : url;
        String title = getMetaTagContent(doc, "og:title");
        String image = getMetaTagContent(doc, "og:image");
        String date = extractPublishedDate(doc);
        String htmlContent = extractNaverNewsContent(doc);

        if (htmlContent == null || htmlContent.isBlank()) return null;

        return SearchNewsDto.builder()
            .url(finalUrl)
            .title(unescape(title))
            .date(parseDateString(date))
            .content(htmlContent)
            .image(image)
            .build();
    }

    private String extractNaverNewsContent(Document doc) {
        String[] contentSelectors = {
            "._article_content", // 1순위: 스포츠, 연예 뉴스
            "#dic_area",         // 2순위: 일반 텍스트 뉴스
            "#articeBody",       // 3순위: 구버전 연예/스포츠 뉴스
            ".go_trans"          // 4순위: TV 뉴스
        };
        Element contentArea = null;
        for (String selector : contentSelectors) {
            contentArea = doc.selectFirst(selector);
            if (contentArea != null) break;
        }
        if (contentArea == null) {
            log.warn("네이버 뉴스 본문 영역을 찾을 수 없음: {}", doc.location());
            return null;
        }
        contentArea.select("script, style, table, .end_photo_org, .img_desc, .vod_player, .ad, .byline, .copyright, [class*='social']").remove();

        return contentArea.text();
    }

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
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<DateTimeFormatter> formatters = List.of(
            DateTimeFormatter.RFC_1123_DATE_TIME,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy. M. d. HH:mm"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd. a H:mm"),
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

    private String requestRawJson(String rawKeyword) {
        String query;
        try {
            query = URLEncoder.encode(rawKeyword, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("검색어 인코딩 실패", e);
        }

        String apiURL = "https://openapi.naver.com/v1/search/news.json?query=" + query + "&display=20&sort=sim";
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Naver-Client-Id", naverApiConfig.getClientId());
        headers.put("X-Naver-Client-Secret", naverApiConfig.getClientSecret());

        HttpURLConnection con = connect(apiURL);
        try {
            con.setRequestMethod("GET");
            headers.forEach(con::setRequestProperty);

            int code = con.getResponseCode();
            InputStream is = (code == HttpURLConnection.HTTP_OK) ? con.getInputStream() : con.getErrorStream();
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

    private static final Pattern IP_ADDRESS_PATTERN = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");

    private boolean isUrl(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new CustomException(ErrorCode.EMPTY_INPUT);
        }
        String trimmedInput = input.trim();
        if (trimmedInput.matches("^[a-zA-Z]+://.*")) return true;
        if (trimmedInput.toLowerCase().startsWith("www.")) return true;
        if (trimmedInput.toLowerCase().startsWith("localhost")) return true;
        if (IP_ADDRESS_PATTERN.matcher(trimmedInput).matches()) return true;
        if (trimmedInput.contains(".") && !trimmedInput.contains(" ")) return true;
        return false;
    }
}