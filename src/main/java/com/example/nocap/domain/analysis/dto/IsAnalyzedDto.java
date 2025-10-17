package com.example.nocap.domain.analysis.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IsAnalyzedDto {

    private boolean isAnalyzed;
    private Long analysisId;
    private String plan;

}
