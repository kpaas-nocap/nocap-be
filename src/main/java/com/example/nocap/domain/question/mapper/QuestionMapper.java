package com.example.nocap.domain.question.mapper;

import com.example.nocap.domain.question.dto.QuestionDetailDto;
import com.example.nocap.domain.question.dto.QuestionRequestDto;
import com.example.nocap.domain.question.entity.Question;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    Question toQuestion(QuestionRequestDto questionRequestDto);

    QuestionDetailDto toQuestionDetailDto(Question question);

}
