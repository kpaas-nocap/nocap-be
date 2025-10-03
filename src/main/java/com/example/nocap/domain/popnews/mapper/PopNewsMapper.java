package com.example.nocap.domain.popnews.mapper;

import com.example.nocap.domain.mainnews.mapper.MainNewsMapper;
import com.example.nocap.domain.news.mapper.NewsMapper;
import com.example.nocap.domain.popnews.dto.PopNewsDetailDto;
import com.example.nocap.domain.popnews.dto.PopNewsDto;
import com.example.nocap.domain.popnews.dto.PopNewsSummaryDto;
import com.example.nocap.domain.popnews.entity.PopNews;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {MainNewsMapper.class, NewsMapper.class})

public interface PopNewsMapper {

    PopNewsDto toPopNewsDto(PopNews popNews);

    PopNewsDetailDto toPopNewsDetailDto(PopNews popNews);
    PopNewsSummaryDto toPopNewsSummaryDto(PopNews popNews);

    List<PopNewsDetailDto> toPopNewsDetailDtoList(List<PopNews> popNewsList);
    List<PopNewsSummaryDto> toPopNewsSummaryDtoList(List<PopNews> popNewsList);

    List<PopNewsDto> toPopNewsDtoList(List<PopNews> popNewsList);

    PopNews toPopNewsEntity(PopNewsDto popNewsDto);

    List<PopNews> toPopNewsEntityList(List<PopNewsDto> popNewsDtoList);

}