package com.kh.wellness.route.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kh.wellness.route.model.vo.RoutePlace;

@Mapper
public interface RouteMapper {

    RoutePlace findPlaceByNo(Long placeNo);

    List<RoutePlace> findPlacesByQuery(@Param("query") String query);

}
