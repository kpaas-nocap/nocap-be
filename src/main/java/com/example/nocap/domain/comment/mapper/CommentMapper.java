package com.example.nocap.domain.comment.mapper;

import com.example.nocap.domain.comment.dto.CommentResponseDto;
import com.example.nocap.domain.comment.entity.Comment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")

public interface CommentMapper {

    CommentResponseDto toCommentResponseDto(Comment comment);

}
