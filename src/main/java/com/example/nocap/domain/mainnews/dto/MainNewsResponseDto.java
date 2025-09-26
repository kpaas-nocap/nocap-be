package com.example.nocap.domain.mainnews.dto;

import jakarta.persistence.Column;
import java.util.List;
import lombok.Data;

@Data
public class MainNewsResponseDto {

    private String url;

    private String title;

    private String date;

    @Column(columnDefinition = "TEXT")
    private String content;

    private List<String> phrases;;

}
