package com.example.nocap.domain.history.dto;

import lombok.Data;

@Data
public class HistoryRequestDto {

    private String url;
    private String title;
    private String content;
    private String date;
    private String image;

}
