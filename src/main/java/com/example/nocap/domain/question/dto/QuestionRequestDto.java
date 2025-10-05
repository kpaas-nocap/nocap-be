package com.example.nocap.domain.question.dto;

import com.example.nocap.domain.question.entity.QuestionCategory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestionRequestDto {

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일은 비어 있을 수 없습니다.")
    private String email;

    @NotNull(message = "문의 분류를 선택해주세요.")
    private QuestionCategory category;

    @NotBlank(message = "문의 내용은 비어 있을 수 없습니다.")
    private String content;
}