package com.example.nocap.domain.comment.repository;

import com.example.nocap.domain.comment.entity.Comment;
import com.example.nocap.domain.comment.entity.CommentRecommendation;
import com.example.nocap.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRecommendationRepository extends
    JpaRepository<CommentRecommendation, Long> {
    // 사용자와 댓글로 기존 추천 기록을 찾는 메소드
    Optional<CommentRecommendation> findByUserAndComment(User user, Comment comment);
}
