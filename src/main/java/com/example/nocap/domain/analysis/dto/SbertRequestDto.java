package com.example.nocap.domain.analysis.dto;

import com.example.nocap.domain.mainnews.dto.MainNewsRequestDto;
import com.example.nocap.domain.news.dto.NewsRequestDto;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SbertRequestDto {

    private String plan;
    private String category;
    private MainNewsRequestDto mainNewsDto;
    private List<NewsRequestDto> newsDtos;

}
