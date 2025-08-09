package com.example.nocap.domain.analysis.dto;

import com.example.nocap.domain.mainnews.dto.MainNewsDto;
import com.example.nocap.domain.news.dto.NewsDto;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SbertRequestDto {

    private String plan;
    private String category;
    private MainNewsDto mainNewsDto;
    private List<NewsDto> newsDtoList;

}
