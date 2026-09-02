package com.kh.wellness.admin.place.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kh.wellness.admin.place.model.dto.AdminPlaceDetailResponse;
import com.kh.wellness.admin.place.model.dto.AdminPlaceListResponse;
import com.kh.wellness.admin.place.model.dto.AdminPlaceUpdateRequest;
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

	int countActivePlace(Long placeNo);

	int updatePlace(@Param("placeNo") Long placeNo, @Param("req") AdminPlaceUpdateRequest req);

	int softDeletePlaceImages(@Param("placeNo") Long placeNo, @Param("imgNos") List<Long> imgNos);

	Integer selectMaxImgOrder(Long placeNo);

	List<PlaceImg> selectPlaceImgList(Long placeNo);

	int updatePlaceImgOrder(@Param("imgNo") Long imgNo, @Param("imgOrder") int imgOrder);

	int softDeletePlaces(@Param("placeNos") List<Long> placeNos);

	int restorePlaces(@Param("placeNos") List<Long> placeNos);
}
