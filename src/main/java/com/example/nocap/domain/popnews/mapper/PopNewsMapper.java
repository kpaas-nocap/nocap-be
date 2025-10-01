package com.example.nocap.domain.popnews.mapper;

import com.example.nocap.domain.mainnews.mapper.MainNewsMapper;
import com.example.nocap.domain.news.mapper.NewsMapper;
import com.example.nocap.domain.popnews.dto.PopNewsDto;
import com.example.nocap.domain.popnews.entity.PopNews;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MainNewsMapper.class, NewsMapper.class})

public interface PopNewsMapper {

    PopNewsDto toPopNewsDto(PopNews popNews);

    List<PopNewsDto> toPopNewsDtoList(List<PopNews> popNewsList);

    @Mapping(target = "popNewsId", ignore = true)
    PopNews toPopNewsEntity(PopNewsDto popNewsDto);

    List<PopNews> toPopNewsEntityList(List<PopNewsDto> popNewsDtoList);

}