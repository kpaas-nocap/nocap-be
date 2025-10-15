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

    @Mapping(source = "history.historyId", target = "id")
    HistorySummaryDto toHistorySummary(History history);

    @Mapping(source = "history.historyId", target = "id")
    HistoryDetailDto toHistoryDetailDto(History history);
}
