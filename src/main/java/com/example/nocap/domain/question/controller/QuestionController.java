package com.example.nocap.domain.question.controller;

import com.example.nocap.auth.dto.response.UserDetail;
import com.example.nocap.domain.question.dto.QuestionAnswerDto;
import com.example.nocap.domain.question.dto.QuestionDetailDto;
import com.example.nocap.domain.question.dto.QuestionRequestDto;
import com.example.nocap.domain.question.dto.QuestionSummaryDto;
import com.example.nocap.domain.question.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequiredArgsConstructor
@RequestMapping("/api/nocap/question")
@Tag(name = "Question", description = "문의사항 관련 API")
public class QuestionController {

    private final QuestionService questionService;

    @Operation(
        summary = "문의사항 저장",
        description = "이메일, 문의 분류, 문의 내용을 통해 문의사항을 저장.",
        responses = { /* ... */ }
    )
    @PostMapping
    public ResponseEntity<QuestionRequestDto> saveQuestion(
        @RequestBody QuestionRequestDto questionRequestDto,
        @AuthenticationPrincipal UserDetail userDetail
    ) {
        return ResponseEntity.ok(questionService.saveQuestion(questionRequestDto, userDetail));
    }

    @Operation(
        summary = "내 문의사항 전체 조회",
        description = "유저가 작성한 문의사항을 전체 조회한다.",
        responses = { /* ... */ }
    )
    @GetMapping
    public ResponseEntity<List<QuestionSummaryDto>> getAllQuestion(@AuthenticationPrincipal UserDetail userDetail) {
        return ResponseEntity.ok(questionService.getAllQuestion(userDetail));
    }

    @Operation(
        summary = "특정 문의사항 조회",
        description = "questionId로 유저가 작성한 특정 문의사항을 조회한다.",
        responses = { /* ... */ }
    )
    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionDetailDto> getQuestion(
        @PathVariable("questionId") Long questionId,
        @AuthenticationPrincipal UserDetail userDetail) {
        return ResponseEntity.ok(questionService.getQuestion(questionId, userDetail));
    }

    @Operation(
        summary = "관리자의 전체 문의사항 조회",
        description = "관리자가 전체 문의사항을 조회한다.",
        responses = { /* ... */ }
    )
    @GetMapping("/admin")
    public ResponseEntity<List<QuestionSummaryDto>> getAllQuestionByAdmin(@AuthenticationPrincipal UserDetail userDetail) {
        return ResponseEntity.ok(questionService.getAllQuestionByAdmin(userDetail));
    }

    @Operation(
        summary = "관리자의 특정 문의사항 조회",
        description = "관리자가 questionId로 특정 문의사항을 조회한다.",
        responses = { /* ... */ }
    )
    @GetMapping("/admin/{questionId}")
    public ResponseEntity<QuestionDetailDto> getQuestionByAdmin(
        @PathVariable("questionId") Long questionId,
        @AuthenticationPrincipal UserDetail userDetail
    ) {
        return ResponseEntity.ok(questionService.getQuestionByAdmin(questionId, userDetail));
    }

    @Operation(
        summary = "관리자의 문의사항 답변 작성",
        description = "관리자가 특정 문의사항에 대해 답변을 작성한다.",
        responses = { /* ... */ }
    )
    @PostMapping("/admin")
    public ResponseEntity<QuestionDetailDto> answerQuestionByAdmin(
        @RequestBody QuestionAnswerDto questionAnswerDto,
        @AuthenticationPrincipal UserDetail userDetail
    ) {
        return ResponseEntity.ok(questionService.answerQuestionByAdmin(questionAnswerDto, userDetail));
    }
}
