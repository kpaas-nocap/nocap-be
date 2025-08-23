package com.example.nocap.domain.analysis.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TitleCategoryDto {

    private String title;
    private String category;

}
