package com.example.nocap.domain.comment.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MyCommentResponseDto {

    private Long analysisId;

    private String content;

    private LocalDateTime date;

    private int recommendation;

    private int nonRecommendation;

}
