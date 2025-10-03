package com.example.nocap.domain.bookmark.service;

import com.example.nocap.auth.dto.response.UserDetail;
import com.example.nocap.domain.analysis.entity.Analysis;
import com.example.nocap.domain.analysis.repository.AnalysisRepository;
import com.example.nocap.domain.bookmark.dto.BookmarkDto;
import com.example.nocap.domain.bookmark.entity.Bookmark;
import com.example.nocap.domain.bookmark.mapper.BookmarkMapper;
import com.example.nocap.domain.bookmark.repository.BookmarkRepository;
import com.example.nocap.domain.user.entity.User;
import com.example.nocap.domain.user.repository.UserRepository;
import com.example.nocap.exception.CustomException;
import com.example.nocap.exception.ErrorCode;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final AnalysisRepository analysisRepository;
    private final UserRepository userRepository;
    private final BookmarkMapper bookmarkMapper;

    @Transactional
    public BookmarkDto saveBookmark(Long analysisId, UserDetail userDetail) {
        Long id = userDetail.getId();
        User user = userRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Analysis analysis = analysisRepository.findById(analysisId)
            .orElseThrow(() -> new CustomException(ErrorCode.ANALYSIS_NOT_FOUND));

        Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserAndAnalysis(user, analysis);

        if (existingBookmark.isPresent()) {
            bookmarkRepository.delete(existingBookmark.get());

            return BookmarkDto.builder()
                .analysisId(analysisId)
                .bookmarked(false)
                .build();
        } else {
            Bookmark bookmark = Bookmark.builder()
                .user(user)
                .analysis(analysis)
                .build();
            bookmarkRepository.save(bookmark);
            return BookmarkDto.builder()
                .analysisId(analysisId)
                .bookmarked(true)
                .build();
        }
    }

    @Transactional(readOnly = true)
    public List<BookmarkDto> getAllBookmark(UserDetail userDetail) {
        Long id = userDetail.getId();
        User user = userRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        List<Bookmark> bookmarkList = user.getBookmarks();
        return bookmarkMapper.toBookmarkDtoList(bookmarkList);
    }
}
