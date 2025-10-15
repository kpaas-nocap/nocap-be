package com.example.nocap.domain.history.mapper;

import com.example.nocap.domain.history.dto.HistoryDetailDto;
import com.example.nocap.domain.history.dto.HistoryRequestDto;
import com.example.nocap.domain.history.dto.HistorySummaryDto;
import com.example.nocap.domain.history.entity.History;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HistoryMapper {

    History toHistory(HistoryRequestDto historyRequestDto);

    @Mapping(target = "id", source = "id")
    HistorySummaryDto toHistorySummary(History history);
    @Mapping(target = "id", source = "id")
    HistoryDetailDto toHistoryDetailDto(History history);
}
