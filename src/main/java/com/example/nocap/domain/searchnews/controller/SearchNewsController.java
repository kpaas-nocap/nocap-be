package com.example.nocap.domain.searchnews.controller;

import com.example.nocap.domain.searchnews.dto.SearchNewsDto;
import com.example.nocap.domain.searchnews.service.SearchNewsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nocap/search")
public class SearchNewsController {

    private final SearchNewsService searchNewsService;

    @GetMapping("category/{category}") // 100 - 104
    public ResponseEntity<List<SearchNewsDto>> getNewsByCategory(@PathVariable("category") int category) {
        return ResponseEntity.ok(searchNewsService.getNewsByCategory(category));
    }

    @GetMapping("/keyword/{keyword}")
    public ResponseEntity<List<SearchNewsDto>> getNewsByKeyword(@PathVariable("keyword") String keyword) {
        return ResponseEntity.ok(searchNewsService.getNewsByKeyword(keyword));
    }
}