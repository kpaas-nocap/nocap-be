package com.example.nocap.domain.analysis.controller;

import com.example.nocap.auth.dto.CustomUserDetails;
import com.example.nocap.domain.analysis.dto.AnalysisDto;
import com.example.nocap.domain.analysis.dto.AnalysisRequestDto;
import com.example.nocap.domain.analysis.dto.AnalysisViewDto;
import com.example.nocap.domain.analysis.dto.SbertRequestDto;
import com.example.nocap.domain.analysis.dto.SbertResponseDto;
import com.example.nocap.domain.analysis.entity.Analysis;
import com.example.nocap.domain.analysis.service.AnalysisProcessService;
import com.example.nocap.domain.analysis.service.AnalysisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nocap/analysis")
public class AnalysisController {

    private final AnalysisProcessService analysisProcessService;
    private final AnalysisService analysisService;

    @PostMapping
    public ResponseEntity<SbertResponseDto> searchNews(@RequestBody AnalysisRequestDto analysisRequestDto) {

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
        Long id)
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