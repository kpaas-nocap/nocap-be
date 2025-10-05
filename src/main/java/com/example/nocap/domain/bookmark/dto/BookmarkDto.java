package com.example.nocap.domain.bookmark.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookmarkDto {

    private Long analysisId;
    private boolean bookmarked;

}
