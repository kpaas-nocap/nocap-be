package com.example.nocap.domain.analysis.dto;

import com.example.nocap.domain.news.dto.NewsRequestDto;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CrawledResponseDto {

    private List<NewsRequestDto> newsDtos;

}