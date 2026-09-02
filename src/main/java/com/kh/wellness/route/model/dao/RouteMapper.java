package com.kh.wellness.route.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kh.wellness.route.model.vo.MapPlace;
import com.kh.wellness.route.model.vo.Place;

@Mapper
public interface RouteMapper {

    Place findPlaceByNo(Long placeNo);

    List<Place> findPlacesByQuery(@Param("query") String query);

    List<MapPlace> findMapPlaces();
}
