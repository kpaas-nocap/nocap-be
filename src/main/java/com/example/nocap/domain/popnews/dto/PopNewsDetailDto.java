package com.example.nocap.domain.popnews.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PopNewsDetailDto {

    private String url;

    private String title;

    private String content;

    private String image;
    private String date;

}
