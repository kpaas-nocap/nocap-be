package com.example.nocap.domain.comment.mapper;

import com.example.nocap.domain.comment.dto.CommentResponseDto;
import com.example.nocap.domain.comment.dto.MyCommentResponseDto;
import com.example.nocap.domain.comment.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")

public interface CommentMapper {

    CommentResponseDto toCommentResponseDto(Comment comment);
    @Mapping(source = "analysis.analysisId", target = "analysisId")
    MyCommentResponseDto toMyCommentResponseDto(Comment comment);

}
