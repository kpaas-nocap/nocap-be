package com.example.nocap.domain.comment.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CommentResponseDto {

    private Long commentId;

    private String username;

    private String content;

    private LocalDateTime date;

    private int recommendation;

    private int nonRecommendation;

}