package com.kh.wellness.place.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.kh.wellness.course.model.dto.PlaceDto;
import com.kh.wellness.place.model.dto.PlaceResponse;

@Mapper
public interface PlaceMapper {

	PlaceDto selectByPlaceNo(Long placeNo);

	List<PlaceResponse> selectPlaces(Long typeDetailNo);

}
