package com.example.nocap.domain.mainnews.mapper;

import com.example.nocap.domain.mainnews.dto.MainNewsDto;
import com.example.nocap.domain.mainnews.entity.MainNews;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MainNewsMapper {

    MainNewsMapper INSTANCE = Mappers.getMapper(MainNewsMapper.class);

    MainNewsDto toMainNewsDto(MainNews mainNews);

}