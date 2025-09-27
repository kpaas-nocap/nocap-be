package com.example.nocap.domain.mainnews.dto;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class MainNewsRequestDto {

    private String url;

    private String title;

    private String date;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String image;

}
