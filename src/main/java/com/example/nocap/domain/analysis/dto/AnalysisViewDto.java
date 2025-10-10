package com.example.nocap.domain.analysis.dto;

import com.example.nocap.domain.analysis.dto.SbertResponseDto.NewsComparisonDto;
import com.example.nocap.domain.comment.dto.CommentResponseDto;
import com.example.nocap.domain.mainnews.dto.MainNewsRequestDto;
import com.example.nocap.domain.mainnews.dto.MainNewsResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class AnalysisViewDto {

    private Long analysisId;
    private String category;
    private String mainNewsTitle; // MainNews의 title
    private Long view;
    private LocalDateTime date;
    private String image; // MainNews의 image url
    private MainNewsResponseDto mainNewsDto;
    private List<NewsComparisonDto> newsComparisonDtos;

    private boolean isBookmarked;

    private List<CommentResponseDto> comments;

}
