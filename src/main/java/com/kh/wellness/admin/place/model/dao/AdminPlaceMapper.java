package com.kh.wellness.admin.place.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kh.wellness.admin.place.model.dto.AdminPlaceDetailResponse;
import com.kh.wellness.admin.place.model.dto.AdminPlaceListResponse;
import com.kh.wellness.admin.place.model.dto.PlaceImageResponse;
import com.kh.wellness.admin.place.model.vo.Place;
import com.kh.wellness.admin.place.model.vo.PlaceImg;

@Mapper
public interface AdminPlaceMapper {

	long countPlaces();

	List<AdminPlaceListResponse> selectPlaces(@Param("offset") Long offset, @Param("size") int size);

	AdminPlaceDetailResponse selectPlaceDetail(Long placeNo);

	List<PlaceImageResponse> selectPlaceImages(Long placeNo);

	int countTypeDetailByNo(Long typeDetailNo);

	int insertPlace(Place place);

	int insertPlaceImg(PlaceImg placeImg);
}
