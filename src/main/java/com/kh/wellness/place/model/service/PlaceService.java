package com.kh.wellness.place.model.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.wellness.course.model.dto.PlaceDto;
import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.place.model.dao.PlaceMapper;
import com.kh.wellness.place.model.dto.MapPlaceResponse;
import com.kh.wellness.place.model.dto.PlaceDetailDto;
import com.kh.wellness.place.model.dto.PlaceDetailResponse;
import com.kh.wellness.place.model.dto.PlaceImageDto;
import com.kh.wellness.place.model.dto.PlaceResponse;
import com.kh.wellness.place.model.dto.PlaceTagDto;
import com.kh.wellness.place.model.dto.PlaceTypeOptionResponse;
import com.kh.wellness.place.model.vo.MapPlace;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceMapper placeMapper;

    public PlaceDto selectByPlaceNo(Long placeNo) {
        PlaceDto place = placeMapper.selectByPlaceNo(placeNo);
        if (place == null) {
            throw new NotFoundException("존재하지 않는 관광지입니다.");
        }
        return place;
    }

    @Transactional(readOnly = true)
    public PlaceDetailResponse getPlaceDetail(Long placeNo) {
        return getPlaceDetail(placeNo, null);
    }

    @Transactional(readOnly = true)
    public PlaceDetailResponse getPlaceDetail(Long placeNo, Long memberNo) {
        PlaceDetailDto placeDetail = placeMapper.selectPlaceDetail(placeNo, memberNo);
        if (placeDetail == null) {
            throw new NotFoundException("해당 장소를 찾을 수 없습니다.");
        }

        List<PlaceImageDto> placeImages = placeMapper.selectPlaceImages(placeNo);
        List<PlaceTagDto> tags = placeMapper.selectPlaceTags(placeNo);

        return PlaceDetailResponse.builder()
                .placeNo(placeDetail.getPlaceNo())
                .placeName(placeDetail.getPlaceName())
                .imageUrl(placeDetail.getImageUrl())
                .placeDescription(placeDetail.getPlaceDescription())
                .addr(placeDetail.getAddr())
                .addrDetail(placeDetail.getAddrDetail())
                .phoneNumber(placeDetail.getPhoneNumber())
                .viewCount(placeDetail.getViewCount())
                .xAxis(placeDetail.getXAxis())
                .yAxis(placeDetail.getYAxis())
                .typeDetailNo(placeDetail.getTypeDetailNo())
                .typeDetail(placeDetail.getTypeDetail())
                .type(placeDetail.getType())
                .isBookmarked("Y".equals(placeDetail.getBookmarkYn()))
                .avgRating(placeDetail.getAvgRating())
                .reviewCount(placeDetail.getReviewCount())
                .placeImages(placeImages)
                .tags(tags)
                .build();
    }

    public List<PlaceResponse> selectPlaces(Long typeDetailNo) {
        List<PlaceResponse> places = placeMapper.selectPlaces(typeDetailNo);
        if (places.isEmpty()) {
            throw new NotFoundException("존재하지 않는 관광지입니다.");
        }
        return places;
    }

    public List<MapPlaceResponse> findMapPlaces() {
        return toMapPlaceResponses(placeMapper.findMapPlaces());
    }

    public List<MapPlaceResponse> findMapPlaces(Long typeNo, Long typeDetailNo, Long tagNo) {
        validateFilterNumber(typeNo, "타입 번호");
        validateFilterNumber(typeDetailNo, "상세 타입 번호");
        validateFilterNumber(tagNo, "태그 번호");
        return toMapPlaceResponses(placeMapper.findMapPlacesByFilters(typeNo, typeDetailNo, tagNo));
    }

    @Transactional(readOnly = true)
    public List<PlaceTypeOptionResponse> findPlaceTypeOptions() {
        return placeMapper.findPlaceTypeOptions();
    }

    @Transactional(readOnly = true)
    public List<PlaceTagDto> findPlaceTagOptions() {
        return placeMapper.findPlaceTagOptions();
    }

    public List<MapPlaceResponse> findMapPlacesByType(String type) {
        return toMapPlaceResponses(placeMapper.findMapPlacesByType(requireFilterValue(type, "타입은 필수입니다.")));
    }

    public List<MapPlaceResponse> findMapPlacesByTag(String tag) {
        return toMapPlaceResponses(placeMapper.findMapPlacesByTag(requireFilterValue(tag, "태그는 필수입니다.")));
    }

    private List<MapPlaceResponse> toMapPlaceResponses(List<MapPlace> places) {
        return places.stream().map(this::toMapPlaceResponse).toList();
    }

    private String requireFilterValue(String value, String requiredMessage) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(requiredMessage);
        }
        return value.trim();
    }

    private void validateFilterNumber(Long value, String fieldName) {
        if (value != null && value <= 0) {
            throw new BadRequestException(fieldName + "는 1 이상이어야 합니다.");
        }
    }

    private MapPlaceResponse toMapPlaceResponse(MapPlace place) {
        return MapPlaceResponse.builder()
                .placeNo(place.getPlaceNo())
                .placeName(place.getPlaceName())
                .placeDescription(place.getPlaceDescription())
                .addr(place.getAddr())
                .addrDetail(place.getAddrDetail())
                .phone(place.getPhone())
                .type(place.getType())
                .typeDetail(place.getTypeDetail())
                .viewCount(place.getViewCount())
                .xAxis(place.getXAxis())
                .yAxis(place.getYAxis())
                .imageUrl(place.getImageUrl())
                .build();
    }

	public List<PlaceResponse> selectPrimaryPlaces(Long typeDetailNo) {
		
		List<PlaceResponse> list = placeMapper.selectPrimaryPlaces(typeDetailNo);
		
		if(list == null || list.isEmpty()) {
			throw new BadRequestException("리스트 조회에 실패하였습니다.");
		}
		
		return list;
	}
}
