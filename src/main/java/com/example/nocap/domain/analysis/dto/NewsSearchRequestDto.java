package com.example.nocap.domain.analysis.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewsSearchRequestDto {

    // Naver News API를 요청할 키워드와 결과에서 제외할 제목(원본 제목)
    private String rawKeyword;
    private String excludeTitle;
    private String excludeUrl;

}
