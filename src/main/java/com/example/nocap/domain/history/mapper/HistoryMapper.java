package com.example.nocap.domain.history.mapper;

import com.example.nocap.domain.history.dto.HistoryDetailDto;
import com.example.nocap.domain.history.dto.HistoryRequestDto;
import com.example.nocap.domain.history.dto.HistorySummaryDto;
import com.example.nocap.domain.history.entity.History;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HistoryMapper {

    History toHistory(HistoryRequestDto historyRequestDto);

    HistorySummaryDto toHistorySummary(History history);

    HistoryDetailDto toHistoryDetailDto(History history);

}
