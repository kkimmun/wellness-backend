package com.kh.wellness.place.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kh.wellness.course.model.dto.PlaceDto;
import com.kh.wellness.place.model.dto.PlaceResponse;
import com.kh.wellness.place.model.dto.PlaceDetailResponse;
import com.kh.wellness.place.model.dto.PlaceImageResponse;
import com.kh.wellness.place.model.vo.MapPlace;

@Mapper
public interface PlaceMapper {

	PlaceDto selectByPlaceNo(Long placeNo);

	List<PlaceResponse> selectPlaces(Long typeDetailNo);

	PlaceDetailResponse selectPlaceDetail(Long placeNo);

	List<PlaceImageResponse> selectPlaceImages(Long placeNo);

    List<MapPlace> findMapPlaces();

    List<MapPlace> findMapPlacesByType(@Param("type") String type);

    List<MapPlace> findMapPlacesByTag(@Param("tag") String tag);

}
