package com.kh.wellness.bookmark.model.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kh.wellness.bookmark.model.vo.PlaceBookmark;

@Mapper
public interface BookmarkMapper {

	int countActivePlace(Long placeNo);

	int countBookmark(@Param("memberNo") Long memberNo, @Param("placeNo") Long placeNo);

	int insertBookmark(PlaceBookmark bookmark);

	int deleteBookmark(@Param("memberNo") Long memberNo, @Param("placeNo") Long placeNo);
}
