package com.example.nocap.domain.popnews.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PopNewsSummaryDto {

    private Long popNewsId;

    private String url;

    private String title;

    private String image;
    private String date;

}
