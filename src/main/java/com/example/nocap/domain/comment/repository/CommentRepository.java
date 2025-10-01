package com.example.nocap.domain.comment.repository;

import com.example.nocap.domain.comment.entity.Comment;
import com.example.nocap.domain.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByUser(User user);
}
