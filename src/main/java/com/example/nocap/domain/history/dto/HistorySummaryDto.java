package com.example.nocap.domain.history.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HistorySummaryDto {

    private Long id;
    private String url;
    private String title;
    private String date;
    private String image;
    private LocalDateTime viewDate;

}
