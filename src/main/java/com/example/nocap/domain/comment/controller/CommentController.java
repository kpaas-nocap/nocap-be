package com.example.nocap.domain.comment.controller;

import com.example.nocap.auth.dto.response.UserDetail;
import com.example.nocap.domain.comment.dto.CommentRequestDto;
import com.example.nocap.domain.comment.dto.CommentResponseDto;
import com.example.nocap.domain.comment.dto.MyCommentResponseDto;
import com.example.nocap.domain.comment.dto.RecommendDto;
import com.example.nocap.domain.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Comment", description = "댓글 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nocap/comment")
public class CommentController {

    private final CommentService commentService;

    @Operation(
        summary = "댓글 작성",
        description = "특정 분석에 대한 댓글 작성",
        responses = { /* ... */ }
    )
    @PostMapping("/create")
    public ResponseEntity<CommentRequestDto> createComment(@RequestBody CommentRequestDto commentRequestDto, @AuthenticationPrincipal
        UserDetail userDetail) {
        return ResponseEntity.ok(commentService.createComment(commentRequestDto, userDetail));
    }

    @Operation(
        summary = "댓글 조회",
        description = "특정 분석에 대한 댓글 조회",
        responses = { /* ... */ }
    )
    @GetMapping("/get/{analysisId}")
    public ResponseEntity<List<CommentResponseDto>> getComment(@PathVariable("analysisId") Long analysisId) {
        return ResponseEntity.ok(commentService.getComment(analysisId));
    }

    @Operation(
        summary = "내 댓글 조회",
        description = "내가 작성한 댓글 조회",
        responses = { /* ... */ }
    )
    @GetMapping("/my")
    public ResponseEntity<List<MyCommentResponseDto>> getMyComment(@AuthenticationPrincipal
    UserDetail userDetail) {
        return ResponseEntity.ok(commentService.getMyComment(userDetail));
    }

    @Operation(
        summary = "내 댓글 삭제",
        description = "내가 작성한 댓글 삭제",
        responses = { /* ... */ }
    )
    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<Void> deleteMyComment(@PathVariable("commentId") Long commentId, @AuthenticationPrincipal
    UserDetail userDetail) {
        commentService.deleteMyComment(commentId, userDetail);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "댓글 추천 비추천 생성",
        description = "댓글 아이디와 추천여부(RECPMMEND/NON_RECOMMEND)를 통해 추천/비추천을 생성",
        responses = { /* ... */ }
    )
    @PostMapping("/recommend/{commentId}")
    public ResponseEntity<CommentResponseDto> RecommendComment(@RequestBody RecommendDto recommendDto, @AuthenticationPrincipal UserDetail userDetail) {
        return ResponseEntity.ok(commentService.recommendComment(recommendDto, userDetail));
    }
}
