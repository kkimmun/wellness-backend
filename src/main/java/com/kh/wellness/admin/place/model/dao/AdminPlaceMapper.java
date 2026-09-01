package com.kh.wellness.admin.place.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kh.wellness.admin.place.model.dto.AdminPlaceListResponse;

@Mapper
public interface AdminPlaceMapper {

	long countPlaces();

	List<AdminPlaceListResponse> selectPlaces(@Param("offset") int offset, @Param("size") int size);
}
