package com.kh.wellness.place.model.dao;

import org.apache.ibatis.annotations.Mapper;

import com.kh.wellness.course.model.dto.PlaceDto;

@Mapper
public interface PlaceMapper {

	PlaceDto selectByPlaceNo(Long placeNo);

}
