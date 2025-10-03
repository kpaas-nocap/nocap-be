package com.example.nocap.domain.bookmark.controller;

import com.example.nocap.auth.dto.response.UserDetail;
import com.example.nocap.domain.bookmark.dto.BookmarkDto;
import com.example.nocap.domain.bookmark.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Bookmark", description = "북마크 관련 API")
@RestController
@RequestMapping("/api/nocap/bookmark")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @Operation(
        summary = "북마크 저장 작성",
        description = "분석 아이디를 통해 내 북마크에 저장",
        responses = { /* ... */ }
    )
    @PostMapping("save/{analysisId}")
    public ResponseEntity<BookmarkDto> saveBookmark(
        @PathVariable("analysisId") Long analysisId,
        @AuthenticationPrincipal UserDetail userDetail) {
        return ResponseEntity.ok(bookmarkService.saveBookmark(analysisId, userDetail));
    }

    @Operation(
        summary = "북마크 조회",
        description = "내 북마크 기록 조회",
        responses = { /* ... */ }
    )
    @GetMapping
    public ResponseEntity<List<BookmarkDto>> getAllBookmark(@AuthenticationPrincipal UserDetail userDetail) {
        return ResponseEntity.ok(bookmarkService.getAllBookmark(userDetail));
    }

}
