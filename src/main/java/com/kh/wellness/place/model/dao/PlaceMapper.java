package com.kh.wellness.place.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kh.wellness.course.model.dto.PlaceDto;
import com.kh.wellness.place.model.dto.PlaceDetailDto;
import com.kh.wellness.place.model.dto.PlaceImageDto;
import com.kh.wellness.place.model.dto.PlaceResponse;
import com.kh.wellness.place.model.dto.PlaceTagDto;
import com.kh.wellness.place.model.vo.MapPlace;

@Mapper
public interface PlaceMapper {

    PlaceDto selectByPlaceNo(Long placeNo);

    PlaceDetailDto selectPlaceDetail(@Param("placeNo") Long placeNo, @Param("memberNo") Long memberNo);

    List<PlaceImageDto> selectPlaceImages(Long placeNo);

    List<PlaceTagDto> selectPlaceTags(Long placeNo);

    List<PlaceResponse> selectPlaces(Long typeDetailNo);

    List<MapPlace> findMapPlaces();

    List<MapPlace> findMapPlacesByType(@Param("type") String type);

    List<MapPlace> findMapPlacesByTag(@Param("tag") String tag);
}
