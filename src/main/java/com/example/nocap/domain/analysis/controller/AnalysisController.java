package com.example.nocap.domain.analysis.controller;

import com.example.nocap.domain.analysis.dto.AnalysisDto;
import com.example.nocap.domain.analysis.dto.AnalysisRequestDto;
import com.example.nocap.domain.analysis.dto.AnalysisViewDto;
import com.example.nocap.domain.analysis.dto.SbertResponseDto;
import com.example.nocap.domain.analysis.service.AnalysisProcessService;
import com.example.nocap.domain.analysis.service.AnalysisService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nocap/analysis")
public class AnalysisController {

    private final AnalysisProcessService analysisProcessService;
    private final AnalysisService analysisService;

    @GetMapping("/healthCheck")
    public ResponseEntity<Void> healthCheck() {
        boolean isHealthy = analysisProcessService.healthCheck();

        if (isHealthy) {
            // 200 OK 상태 코드로 응답하되 본문은 비움
            return ResponseEntity.ok().build();
        } else {
            // 503 Service Unavailable 상태 코드로 응답
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    @PostMapping
    public ResponseEntity<SbertResponseDto> analysis (@RequestBody AnalysisRequestDto analysisRequestDto) {

        SbertResponseDto sbertRequestDto = analysisProcessService.analyzeUrlAndPrepareRequest(
            analysisRequestDto);

        return ResponseEntity.ok(sbertRequestDto);
    }

    @GetMapping
    public ResponseEntity<List<AnalysisDto>> getAllAnalysis() {
        return ResponseEntity.ok(analysisService.getAllAnalysis());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnalysisViewDto> getAnalysisById(@PathVariable Long id) {
        return ResponseEntity.ok(analysisService.getAnalysisById(id));
    }

    @GetMapping("category/{category}")
    public ResponseEntity<List<AnalysisDto>> getAnalysisByCategory(@PathVariable String category) {
        return ResponseEntity.ok(analysisService.getAnalysisByCategory(category));
    }

    @GetMapping("/my/{id}")
    public ResponseEntity<List<AnalysisDto>> getAnalysisByUserId(
        //@AuthenticationPrincipal CustomUserDetails customUserDetails)
        @PathVariable Long id)
        {
        //return ResponseEntity.ok(analysisService.getAnalysisByUserId(customUserDetails));
        return ResponseEntity.ok(analysisService.getAnalysisByUserId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnalysisById(@PathVariable Long id) {
        analysisService.deleteAnalysisById(id);
        return ResponseEntity.noContent().build();
    }

}