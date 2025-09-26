package com.example.nocap.domain.searchnews.controller;

import com.example.nocap.domain.searchnews.dto.SearchNewsDto;
import com.example.nocap.domain.searchnews.service.SearchNewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "SearchNews", description = "뉴스 검색 API") // ✨ 1. @Tag 어노테이션 이동
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nocap/search")
public class SearchNewsController {

    private final SearchNewsService searchNewsService;

    @Operation(
        summary = "카테고리별 뉴스 검색",
        description = "카테고리 번호 100-104에 따라 정치, 경제, 사회, 생활/문화, 세계, IT/과학 뉴스 조회.",
        parameters = { @Parameter(name = "category", description = "뉴스 카테고리", required = true, example = "102"),},
        responses = { /* ... */ }
    )
    @GetMapping("category/{category}") // 100 - 104
    public ResponseEntity<List<SearchNewsDto>> getNewsByCategory(@PathVariable("category") int category) {
        return ResponseEntity.ok(searchNewsService.getNewsByCategory(category));
    }

    @Operation(
        summary = "키워드별 뉴스 검색",
        description = "입력된 키워드를 통한 뉴스 조회.",
        parameters = { @Parameter(name = "keyword", description = "뉴스 검색 키워드", required = true, example = "축구"),},
        responses = { /* ... */ }
    )
    @GetMapping("/keyword/{keyword}")
    public ResponseEntity<List<SearchNewsDto>> getNewsByKeyword(@PathVariable("keyword") String keyword) {
        return ResponseEntity.ok(searchNewsService.getNewsByKeyword(keyword));
    }
}