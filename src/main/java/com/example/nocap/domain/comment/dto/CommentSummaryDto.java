package com.example.nocap.domain.comment.dto;

import lombok.Data;

@Data
public class CommentSummaryDto {

    private Long commentId;

    private Long userId;

    private String content;

}
