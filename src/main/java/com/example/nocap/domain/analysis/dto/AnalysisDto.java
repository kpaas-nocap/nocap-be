package com.example.nocap.domain.analysis.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AnalysisDto {

    private Long analysisId;
    private String category;
    private String mainNewsTitle; // MainNews의 title
    private Long view;
    private LocalDateTime date;
    private String image; // MainNews의 image url

}
