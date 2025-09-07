package com.example.nocap.domain.analysis.service;

import com.example.nocap.domain.mainnews.entity.MainNews;
import com.example.nocap.exception.CustomException;
import com.example.nocap.exception.ErrorCode;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(5_000)
                .get();

            // 기사 여부 판별 로직
            if (!isArticleByConfidenceScore(doc)) {
                // 변경점 1: 기사가 아닐 때 Exception 발생
                throw new CustomException(ErrorCode.NOT_A_NEWS_ARTICLE);
            }

            String canonicalUrl = "";
            Element canonicalLink = doc.selectFirst("link[rel=canonical]");
            if (canonicalLink != null && canonicalLink.hasAttr("href")) {
                canonicalUrl = canonicalLink.attr("href");
            } else {
                canonicalUrl = normalizeUrl(url);
            }

            Element ogImageElement = doc.selectFirst("meta[property=og:image]");
            String ogImage = (ogImageElement != null) ? ogImageElement.attr("content") : "";

            Article article = new Readability4J(url, doc.html()).parse();

            String htmlContent = article.getContent();
            String plainContent = Jsoup.parse(htmlContent).text();

            return MainNews.builder()
                .url(url)
                .canonicalUrl(canonicalUrl)
                .title(article.getTitle())
                .content(plainContent)
                .image(ogImage)
                .build();

        } catch (IOException e) {
            // 변경점 2: Jsoup 연결 실패 시 Exception 발생
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }
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