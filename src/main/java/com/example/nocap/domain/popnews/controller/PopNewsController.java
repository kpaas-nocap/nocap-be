package com.example.nocap.domain.popnews.controller;

import com.example.nocap.domain.popnews.dto.PopNewsDto;
import com.example.nocap.domain.popnews.service.PopNewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nocap/popnews")
public class PopNewsController {

    private final PopNewsService popNewsService;

    @Operation(
        summary = "인기뉴스 조회",
        description = "하루 주기로 갱신되는 인기뉴스 조회",
        responses = { /* ... */ }
    )
    @GetMapping
    public ResponseEntity<List<PopNewsDto>> getPopNews() {
        return ResponseEntity.ok(popNewsService.getPopNews());
    }

}