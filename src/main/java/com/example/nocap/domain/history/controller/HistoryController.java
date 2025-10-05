package com.example.nocap.domain.history.controller;

import com.example.nocap.auth.dto.response.UserDetail;
import com.example.nocap.domain.history.dto.HistoryDetailDto;
import com.example.nocap.domain.history.dto.HistoryRequestDto;
import com.example.nocap.domain.history.dto.HistorySummaryDto;
import com.example.nocap.domain.history.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nocap/history")
@RequiredArgsConstructor
@Tag(name = "History", description = "뉴스 조회기록 관련 API")
public class HistoryController {

    private final HistoryService historyService;

    @Operation(
        summary = "조회기록 저장",
        description = "로그인된 사용자가 뉴스를 조회할 때 해당 뉴스의 전체 정보를 저장",
        responses = { /* ... */ }
    )
    @PostMapping("/record")
    public ResponseEntity<HistoryRequestDto> saveHistory(
        @RequestBody HistoryRequestDto historyRequestDto,
        @AuthenticationPrincipal UserDetail userDetail) {
        return ResponseEntity.ok(historyService.saveHistory(historyRequestDto, userDetail));
    }

    @Operation(
        summary = "전체 조회기록 조회",
        description = "로그인된 사용자가 자신의 최신 뉴스 조회기록 10개에 대한 정보 조회",
        responses = { /* ... */ }
    )
    @GetMapping
    public ResponseEntity<List<HistorySummaryDto>> getAllHistory(@AuthenticationPrincipal UserDetail userDetail) {
        return ResponseEntity.ok(historyService.getAllHistory(userDetail));
    }

    @Operation(
        summary = "특정 조회기록 조회",
        description = "로그인된 사용자가 historyId를 통해 자신의 특정 뉴스 조회기록 한 개를 조회 ",
        responses = { /* ... */ }
    )
    @GetMapping("/{historyId}")
    public ResponseEntity<HistoryDetailDto> getHistory(
        @PathVariable("historyId") Long historyId,
        @AuthenticationPrincipal UserDetail userDetail) {
        return ResponseEntity.ok(historyService.getHistory(historyId, userDetail));
    }

}
