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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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