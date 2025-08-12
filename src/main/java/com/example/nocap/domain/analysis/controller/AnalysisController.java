package com.example.nocap.domain.analysis.controller;

import com.example.nocap.domain.analysis.dto.SbertRequestDto;
import com.example.nocap.domain.analysis.service.AnalysisProcessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisProcessService analysisProcessService;

    public AnalysisController(AnalysisProcessService analysisProcessService) {
        this.analysisProcessService = analysisProcessService;
    }

    @GetMapping
    public ResponseEntity<SbertRequestDto> searchNews(@RequestParam("url") String url) {

        SbertRequestDto sbertRequestDto = analysisProcessService.analyzeUrlAndPrepareRequest(url);

        // SbertRequestDto를 lambda 호출로 보내기
        // 유사도, 비교요약을 담은 데이터 반환받기
        // 분석, 메인뉴스, 뉴스를 각 레포에 저장

        return ResponseEntity.ok(sbertRequestDto);
    }
}