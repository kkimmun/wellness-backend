package com.kh.wellness.place.model.dao;

import org.apache.ibatis.annotations.Mapper;

import com.kh.wellness.admin.place.model.vo.Place;

@Mapper
public interface PlaceMapper {

	Place selectByPlaceNo(Long placeNo);
}
