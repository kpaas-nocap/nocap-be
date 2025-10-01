package com.example.nocap.domain.searchnews.service;

import com.example.nocap.domain.analysis.service.ArticleExtractionService;
import com.example.nocap.domain.mainnews.entity.MainNews;
import com.example.nocap.domain.searchnews.dto.NewsSearchResponseDto;
import com.example.nocap.domain.searchnews.dto.SearchNewsDto;
import com.example.nocap.exception.CustomException;
import com.example.nocap.exception.ErrorCode;
import com.example.nocap.global.NaverApiConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.dankito.readability4j.Article;
import net.dankito.readability4j.Readability4J;
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

    // 카테고리별 뉴스 검색
    public List<SearchNewsDto> getNewsByCategory(int category) {
        String sectionUrl = String.format(SECTION_URL_TEMPLATE, category);

        try {
            Document sectionDoc = Jsoup.connect(sectionUrl)
                .userAgent("Mozilla/5.0")
                .timeout(5_000)
                .get();

            List<String> urls = sectionDoc.select("a[href^=\"https://n.news.naver.com/mnews/article\"]")
                .stream()
                .map(a -> a.absUrl("href"))
                .distinct()
                .filter(u -> !u.contains("/article/comment/"))
                .toList();

            // 병렬 스트림으로 각 URL을 파싱하여 List<SearchNewsDto>를 직접 반환
            return urls.parallelStream()
                .map(url -> {
                    try {
                        return parse(url); // parse 메소드가 이제 SearchNewsDto를 반환
                    } catch (IOException | IllegalStateException e) {
                        System.err.println("파싱 실패: " + url + " / " + e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        } catch (IOException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "섹션 크롤링 실패: " + e.getMessage(), e
            );
        }
    }

    public SearchNewsDto parse(String url) throws IOException {
        Document doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0")
            .timeout(5_000)
            .get();

        String title = null;
        String[] titleSelectors = {"meta[property=og:title]", "#articleTitle", "h2#title_area", ".media_end_head_headline span"};
        for (String sel : titleSelectors) {
            Element e = doc.selectFirst(sel);
            if (e != null) {
                title = e.hasAttr("content") ? e.attr("content") : e.text();
                break;
            }
        }
        if (title == null) throw new IllegalStateException("제목을 찾을 수 없습니다: " + url);

        String date = null;
        String[] dateSelectors = {"meta[property=article:published_time]", "span.t11", "span._ARTICLE_DATE_TIME"};
        for (String sel : dateSelectors) {
            Element e = doc.selectFirst(sel);
            if (e != null) {
                if (e.hasAttr("content")) date = e.attr("content");
                else if (e.hasAttr("data-date-time")) date = e.attr("data-date-time");
                else date = e.text();
                break;
            }
        }
        if (date == null) throw new IllegalStateException("날짜를 찾을 수 없습니다: " + url);

        Element bodyEl = null;
        String[] bodySelectors = {"#articleBodyContents", "#newsEndContents", "#dic_area", ".newsct_article", ".article_body"};
        for (String sel : bodySelectors) {
            bodyEl = doc.selectFirst(sel);
            if (bodyEl != null) break;
        }
        if (bodyEl == null) throw new IllegalStateException("본문 요소를 찾을 수 없습니다: " + url);

        bodyEl.select("script, .ad_banner, .link_news, noscript, div[style]").remove();
        String content = bodyEl.text().trim();

        String image = null;
        Element metaImg = doc.selectFirst("meta[property=og:image]");
        if (metaImg != null && metaImg.hasAttr("content")) {
            image = metaImg.attr("content");
        } else {
            Element imgEl = bodyEl.selectFirst(".end_photo_org img, .nbd_im_w img, img[src]");
            if (imgEl != null && imgEl.hasAttr("src")) {
                image = imgEl.absUrl("src");
            }
        }

        return SearchNewsDto.builder()
            .url(url)
            .title(title)
            .date(date)
            .content(content)
            .image(image)
            .build();
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
                        .date(parseDateString(item.getPubDate())) // 날짜 변환
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


    // URL로 뉴스 검색
    public List<SearchNewsDto> getNewsByUrl(String url) {
        return List.of(extract(url));
    }

    public SearchNewsDto extract(String url) {
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

            if (htmlContent == null || htmlContent.trim().isEmpty()) {
                throw new CustomException(ErrorCode.NEWS_EXTRACTING_ERROR);
            }
            return SearchNewsDto.builder()
                .url(url)
                .title(finalTitle)
                .content(htmlContent)
                .image(ogImage)
                .date(pubDate)
                .build();

        } catch (IOException e) {
            throw new CustomException(ErrorCode.NEWS_EXTRACTING_ERROR);
        }
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

    public List<SearchNewsDto> getNewsBySearch(String search) {
        if (isUrl(search)) {
            return getNewsByUrl(search);
        } else {
            return getNewsByKeyword(search);
        }
    }

    private static final Pattern IP_ADDRESS_PATTERN =
        Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");

    public static boolean isUrl(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new CustomException(ErrorCode.EMPTY_INPUT);
        }

        String trimmedInput = input.trim();

        // 1. 형식 검사: URL의 명확한 특징이 있는가?
        if (trimmedInput.matches("^[a-zA-Z]+://.*")) {
            return true;
        }
        if (trimmedInput.toLowerCase().startsWith("www.")) {
            return true;
        }
        if (trimmedInput.toLowerCase().startsWith("localhost")) {
            return true;
        }
        if (IP_ADDRESS_PATTERN.matcher(trimmedInput).matches()) {
            return true;
        }
        if (trimmedInput.contains(".") && !trimmedInput.contains(" ")) {
            return true;
        }

        // 2. 위의 모든 URL 조건에 해당하지 않으면 검색어 (false)
        return false;
    }
}