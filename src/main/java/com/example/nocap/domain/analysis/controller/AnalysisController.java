package com.example.nocap.domain.analysis.controller;

import com.example.nocap.auth.dto.response.UserDetail;
import com.example.nocap.domain.analysis.dto.AnalysisDto;
import com.example.nocap.domain.analysis.dto.AnalysisRequestDto;
import com.example.nocap.domain.analysis.dto.AnalysisViewDto;
import com.example.nocap.domain.analysis.dto.SbertResponseDto;
import com.example.nocap.domain.analysis.service.AnalysisProcessService;
import com.example.nocap.domain.analysis.service.AnalysisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Analysis", description = "분석 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nocap/analysis")
public class AnalysisController {

    private final AnalysisProcessService analysisProcessService;
    private final AnalysisService analysisService;

    @Operation(
        summary = "FastAPI 헬스체크",
        description = "FastAPI 서버와의 연결을 확인.",
        responses = { /* ... */ }
    )
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

    @Operation(
        summary = "분석 수행",
        description = "사용자의 id와 분석할 기사를통해 분석을 수행.",
        parameters = { @Parameter(name = "userId", description = "사용자 ID", required = true, example = "1"),
                       @Parameter(name = "SearchNewsDto", description = "분석할 뉴스", required = true, example = "")},
        responses = { /* ... */ }
    )
    @PostMapping
    public ResponseEntity<SbertResponseDto> analysis (
        @RequestBody AnalysisRequestDto analysisRequestDto,
        @AuthenticationPrincipal UserDetail userDetail)
        throws JsonProcessingException {

        SbertResponseDto sbertRequestDto = analysisProcessService.analyzeUrlAndPrepareRequest(
            analysisRequestDto, userDetail);

        return ResponseEntity.ok(sbertRequestDto);
    }

    @Operation(
        summary = "모든 분석 조회",
        description = "수행된 모든 분석을 조회.",
        responses = { /* ... */ }
    )
    @GetMapping
    public ResponseEntity<List<AnalysisDto>> getAllAnalysis() {
        return ResponseEntity.ok(analysisService.getAllAnalysis());
    }

    @Operation(
        summary = "특정 분석 조회",
        description = "분석 id를 통해 특정 분석 하나를 조회.",
        parameters = { @Parameter(name = "id", description = "분석 ID", required = true, example = "1"),},
        responses = { /* ... */ }
    )
    @GetMapping("/{id}")
    public ResponseEntity<AnalysisViewDto> getAnalysisById(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetail userDetail) {
        return ResponseEntity.ok(analysisService.getAnalysisById(id, userDetail));
    }

    @Operation(
        summary = "카테고리별 분석 조회",
        description = "요청된 카테고리에 맞는 분석을 조회.",
        parameters = { @Parameter(name = "category", description = "분석 카테고리", required = true, example = "사회"),},
        responses = { /* ... */ }
    )
    @GetMapping("category/{category}")
    public ResponseEntity<List<AnalysisDto>> getAnalysisByCategory(@PathVariable String category) {
        return ResponseEntity.ok(analysisService.getAnalysisByCategory(category));
    }

    @Operation(
        summary = "내 분석 조회",
        description = "내가 수행한 분석 조회.",
        responses = { /* ... */ }
    )
    @GetMapping("/my")
    public ResponseEntity<List<AnalysisDto>> getAnalysisByUserId(@AuthenticationPrincipal UserDetail userDetail) { 
        return ResponseEntity.ok(analysisService.getAnalysisByUserId(userDetail));
    }

    @Operation(
        summary = "특정 분석 삭제",
        description = "분석 id를 통해 특정 분석을 삭제.",
        parameters = { @Parameter(name = "id", description = "분석 ID", required = true, example = "1"),},
        responses = { /* ... */ }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnalysisById(@PathVariable Long id) {
        analysisService.deleteAnalysisById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "분석 이력 확인",
        description = "입력한 url과 메인뉴스의 canonicalUrl을 비교하여 분석 이력 확인",
        parameters = { @Parameter(name = "id", description = "분석 ID", required = true, example = "1"),},
        responses = { /* ... */ }
    )
    @GetMapping("/check")
    public ResponseEntity<Boolean> isAnalyzed(@RequestParam("url") String url) {
        return ResponseEntity.ok(analysisService.isAnalyzed(url));
    }
}