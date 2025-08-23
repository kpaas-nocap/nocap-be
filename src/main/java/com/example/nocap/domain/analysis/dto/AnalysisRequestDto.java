package com.example.nocap.domain.analysis.dto;

import lombok.Data;

@Data
public class AnalysisRequestDto {
    private String url;
    private String plan;
    private Long userId;
}
