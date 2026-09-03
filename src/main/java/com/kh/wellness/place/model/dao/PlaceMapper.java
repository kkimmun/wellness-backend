package com.kh.wellness.place.model.dao;

import com.kh.wellness.course.model.dto.PlaceDto;
import com.kh.wellness.place.model.dto.PlaceDetailDto;
import com.kh.wellness.place.model.dto.PlaceImageDto;
import com.kh.wellness.place.model.dto.PlaceTagDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import com.kh.wellness.course.model.dto.PlaceDto;
import com.kh.wellness.place.model.dto.PlaceResponse;


@Mapper
public interface PlaceMapper {

    
    PlaceDto selectByPlaceNo(Long placeNo);

    // 고객님 코드 (상세보기)
    PlaceDetailDto selectPlaceDetail(@Param("placeNo") Long placeNo, @Param("memberNo") Long memberNo);
    List<PlaceImageDto> selectPlaceImages(@Param("placeNo") Long placeNo);
    List<PlaceTagDto> selectPlaceTags(@Param("placeNo") Long placeNo);
	List<PlaceResponse> selectPlaces(Long typeDetailNo);


}
