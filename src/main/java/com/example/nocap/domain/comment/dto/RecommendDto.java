package com.example.nocap.domain.comment.dto;

import lombok.Data;

@Data
public class RecommendDto {

    private Long commentId;
    private RecommendType action;

}
