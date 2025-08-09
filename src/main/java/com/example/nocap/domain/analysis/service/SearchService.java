package com.example.nocap.domain.analysis.service;

import com.example.nocap.domain.analysis.config.NaverApiConfig;
import com.example.nocap.domain.analysis.dto.NewsSearchRequestDto;
import com.example.nocap.domain.analysis.dto.NewsSearchResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private final NaverApiConfig config;
    private final ObjectMapper mapper;

    public SearchService(NaverApiConfig config, ObjectMapper mapper) {
        this.config = config;
        this.mapper = mapper;
    }

    public NewsSearchResponseDto searchNewsDto(NewsSearchRequestDto newsSearchRequestDto) {
        // 1. 11개 가져오기
        String json = requestRawJson(newsSearchRequestDto.getRawKeyword());
        // 2. JSON → DTO
        NewsSearchResponseDto newsSearchResponseDto = jsonToDto(json);
        /// 3. 제목 HTML 태그 제거
        sanitizeItems(newsSearchResponseDto);
        // 4. excludeTitle 검사 및 최종 DTO 반환
        return excludeOrTrim(newsSearchResponseDto, newsSearchRequestDto.getExcludeTitle());
    }
    private void sanitizeItems(NewsSearchResponseDto newsSearchResponseDto) {
        if (newsSearchResponseDto.getItems() == null) return;

        for (NewsSearchResponseDto.Item item : newsSearchResponseDto.getItems()) {
            // 제목에서 태그 제거
            if (item.getTitle() != null) {
                String plainTitle = Jsoup.parse(item.getTitle()).text();
                item.setTitle(plainTitle);
            }
            // (선택) description 에서도 태그 제거가 필요하면 아래 추가
            if (item.getDescription() != null) {
                String plainDesc = Jsoup.parse(item.getDescription()).text();
                item.setDescription(plainDesc);
            }
        }
    }

    private NewsSearchResponseDto jsonToDto(String json) {
        try {
            return mapper.readValue(json, NewsSearchResponseDto.class);
        } catch (IOException e) {
            throw new RuntimeException("JSON → DTO 변환 실패", e);
        }
    }

    private String requestRawJson(String rawKeyword) {
        String query;
        try {
            query = URLEncoder.encode(rawKeyword, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("검색어 인코딩 실패", e);
        }

        String apiURL = "https://openapi.naver.com/v1/search/news.json"
            + "?query=" + query
            + "&display=" + 11
            + "&start=1"
            + "&sort=sim";

        Map<String, String> headers = new HashMap<>();
        headers.put("X-Naver-Client-Id",     config.getClientId());
        headers.put("X-Naver-Client-Secret", config.getClientSecret());

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

    private NewsSearchResponseDto excludeOrTrim(NewsSearchResponseDto resp, String excludeTitle) {
        // 1. 원본 리스트 복사
        List<NewsSearchResponseDto.Item> items = new ArrayList<>(resp.getItems());

        // 2. HTML 태그 제거한 텍스트로 비교해서 제거
        boolean removed = items.removeIf(item -> {
            // <b>태그 등 HTML 제거
            String plainTitle = Jsoup.parse(item.getTitle()).text();
            return plainTitle.equals(excludeTitle);
        });

        // 3. 중복 제거된 게 없고, 11개 이상일 때 마지막 하나만 제거
        if (!removed && items.size() > 10) {
            items.remove(items.size() - 1);
        }

        // 4. DTO 업데이트
        resp.setItems(items);
        resp.setDisplay(items.size());
        return resp;
    }
}