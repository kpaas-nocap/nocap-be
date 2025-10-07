package com.example.nocap.domain.analysis.service;

import com.example.nocap.domain.analysis.service.parser.SiteSpecificParser;
import com.example.nocap.domain.mainnews.entity.MainNews;
import com.example.nocap.exception.CustomException;
import com.example.nocap.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j

public class ArticleExtractionService {

    private final List<SiteSpecificParser> parsers;

    public MainNews extract(String url) {
        log.info("Article Extraction Eroc ess for URL: {}", url);
        try {
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(5000)
                .get();

            // URL에 맞는 전문 파서를 찾아서 실행
            for (SiteSpecificParser parser : parsers) {
                if (parser.canParse(url)) {
                    return parser.extract(doc, url);
                }
            }

            throw new CustomException(ErrorCode.NEWS_EXTRACTING_ERROR);

        } catch (IOException e) {
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }
}