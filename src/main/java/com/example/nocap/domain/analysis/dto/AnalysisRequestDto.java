package com.example.nocap.domain.analysis.dto;

import com.example.nocap.domain.searchnews.dto.SearchNewsDto;
import lombok.Data;

@Data
public class AnalysisRequestDto {
    private String plan;
    private SearchNewsDto searchNewsDto;
}
