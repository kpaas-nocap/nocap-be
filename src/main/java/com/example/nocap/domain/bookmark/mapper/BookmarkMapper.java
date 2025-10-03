package com.example.nocap.domain.bookmark.mapper;

import com.example.nocap.domain.bookmark.dto.BookmarkDto;
import com.example.nocap.domain.bookmark.entity.Bookmark;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")

public interface BookmarkMapper {

    @Mapping(source = "analysis.analysisId", target = "analysisId")
    @Mapping(target = "bookmarked", constant = "true")
    BookmarkDto toBookmarkDto(Bookmark bookmark);

    List<BookmarkDto> toBookmarkDtoList(List<Bookmark> bookmarkList);

}
