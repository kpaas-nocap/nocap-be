package com.example.nocap.domain.searchnews.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchNewsDto {

    private String url;
    private String title;
    private String content;
    private String date;
    private String image;

}
