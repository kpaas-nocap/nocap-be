package com.example.nocap.domain.comment.service;

import com.example.nocap.auth.dto.response.UserDetail;
import com.example.nocap.domain.analysis.entity.Analysis;
import com.example.nocap.domain.analysis.repository.AnalysisRepository;
import com.example.nocap.domain.comment.dto.CommentRequestDto;
import com.example.nocap.domain.comment.dto.CommentResponseDto;
import com.example.nocap.domain.comment.dto.MyCommentResponseDto;
import com.example.nocap.domain.comment.dto.RecommendDto;
import com.example.nocap.domain.comment.dto.RecommendType;
import com.example.nocap.domain.comment.entity.Comment;
import com.example.nocap.domain.comment.entity.CommentRecommendation;
import com.example.nocap.domain.comment.mapper.CommentMapper;
import com.example.nocap.domain.comment.repository.CommentRecommendationRepository;
import com.example.nocap.domain.comment.repository.CommentRepository;
import com.example.nocap.domain.user.entity.User;
import com.example.nocap.domain.user.repository.UserRepository;
import com.example.nocap.exception.CustomException;
import com.example.nocap.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.mapstruct.control.MappingControl.Use;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final AnalysisRepository analysisRepository;
    private final CommentMapper commentMapper;
    private final UserRepository userRepository;
    private final CommentRecommendationRepository commentRecommendationRepository;

    public CommentRequestDto createComment(CommentRequestDto commentRequestDto, UserDetail userDetail) {

        Long id = userDetail.getId();
        Long analysisId = commentRequestDto.getAnalysisId();

        User user = userRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Optional<Analysis> optionalAnalysis = Optional.of(analysisRepository.findById(analysisId)
            .orElseThrow(() -> new CustomException(ErrorCode.ANALYSIS_NOT_FOUND)));
        Analysis analysis = optionalAnalysis.get();

        Comment comment = Comment.builder()
            .user(user)
            .date(LocalDateTime.now())
            .content(commentRequestDto.getContent())
            .analysis(analysis)
            .build();
        analysis.addComment(comment);
        analysisRepository.save(analysis);
        return commentRequestDto;
    }


    public List<CommentResponseDto> getComment(Long analysisId) {
        Optional<Analysis> optionalAnalysis = Optional.of(analysisRepository.findById(analysisId)
            .orElseThrow(() -> new CustomException(ErrorCode.ANALYSIS_NOT_FOUND)));
        Analysis analysis = optionalAnalysis.get();

        return analysis.getComments().stream()
            .map(commentMapper::toCommentResponseDto) // 각 Comment를 DTO로 매핑
            .toList();
    }

    public List<MyCommentResponseDto> getMyComment(UserDetail userDetail) {
        List<Comment> commentList = commentRepository.findByUser(userDetail.getUser());
        return commentList.stream()
            .map(commentMapper::toMyCommentResponseDto)
            .toList();
    }

    public void deleteMyComment(Long commentId, UserDetail userDetail) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUser().getId().equals(userDetail.getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Analysis analysis = comment.getAnalysis();
        if (analysis != null) {
            analysis.getComments().remove(comment);
        }

        commentRepository.delete(comment);
    }

    public CommentResponseDto recommendComment(RecommendDto recommendDto, UserDetail userDetail) {

        User user = userRepository.findById(userDetail.getId())
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Comment comment = commentRepository.findById(recommendDto.getCommentId())
            .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        Optional<CommentRecommendation> existingRec = commentRecommendationRepository.findByUserAndComment(user, comment);

        if (existingRec.isPresent()) {
            // [CASE A: 이미 추천/비추천 기록이 있는 경우]
            CommentRecommendation recommendation = existingRec.get();

            if (recommendation.getRecommendType() == recommendDto.getAction()) {
                // a) 같은 버튼을 다시 누름 -> 취소
                commentRecommendationRepository.delete(recommendation);
                if (recommendDto.getAction() == RecommendType.RECOMMEND) {
                    comment.setRecommendation(comment.getRecommendation() - 1);
                } else {
                    comment.setNonRecommendation(comment.getNonRecommendation() - 1);
                }
            } else {
                // b) 다른 버튼을 누름 -> 변경 (예: 추천 -> 비추천)
                if (recommendDto.getAction() == RecommendType.RECOMMEND) {
                    comment.setRecommendation(comment.getRecommendation() + 1);
                    comment.setNonRecommendation(comment.getNonRecommendation() - 1);
                } else {
                    comment.setRecommendation(comment.getRecommendation() - 1);
                    comment.setNonRecommendation(comment.getNonRecommendation() + 1);
                }
                recommendation.setRecommendType(recommendDto.getAction());
            }
        } else {
            // [CASE B: 추천/비추천 기록이 없는 경우 (신규)]
            CommentRecommendation newRec = new CommentRecommendation(user, comment, recommendDto.getAction());
            commentRecommendationRepository.save(newRec);

            if (recommendDto.getAction() == RecommendType.RECOMMEND) {
                comment.setRecommendation(comment.getRecommendation() + 1);
            } else {
                comment.setNonRecommendation(comment.getNonRecommendation() + 1);
            }
        }

        return commentMapper.toCommentResponseDto(comment);
    }
}
