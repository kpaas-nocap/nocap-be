package com.example.nocap.domain.mainnews.dto;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class MainNewsDto {

    private String url;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;


}
