package com.example.nocap.domain.analysis.service;

import org.springframework.stereotype.Service;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UrlNormalizationService {

    /**
     * 입력된 URL 문자열을 정규화하여 Canonical URL을 생성합니다.
     * @param urlString 원본 URL 문자열
     * @return 정규화된 URL 문자열
     */
    public String normalize(String urlString) {
        if (urlString == null) {
            return null;
        }
        try {
            URL url = new URL(urlString);
            String path = url.getPath();
            String query = url.getQuery();

            // 1. 쿼리 파라미터가 없으면 그대로 반환
            if (query == null || query.isEmpty()) {
                return url.getProtocol() + "://" + url.getHost() + path;
            }

            // 2. 기사 식별에 필수적인 파라미터 목록
            List<String> essentialParams = List.of(
                "article_id", "id", "no", "idx", "idxno", "pno", "newsid", "materialId" // ✨ idxno 추가
            );

            // 3. 필수 파라미터만 남기고 정렬하여 새로운 쿼리 문자열 생성
            String preservedQuery = Arrays.stream(query.split("&"))
                .filter(param -> essentialParams.stream().anyMatch(key -> param.startsWith(key + "=")))
                .sorted()
                .collect(Collectors.joining("&"));

            String baseUrl = url.getProtocol() + "://" + url.getHost() + path;

            return preservedQuery.isEmpty() ? baseUrl : baseUrl + "?" + preservedQuery;

        } catch (MalformedURLException e) {
            // URL 형식이 아닐 경우 원본 문자열 그대로 반환
            return urlString;
        }
    }
}
