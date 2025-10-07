package com.example.nocap.domain.question.dto;

import com.example.nocap.domain.question.entity.QuestionCategory;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class QuestionDetailDto {

    private QuestionCategory category;
    private String content;
    private LocalDateTime createdAt;
    private String answer;
    private LocalDateTime answeredAt;

}
