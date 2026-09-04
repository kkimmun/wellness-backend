package com.kh.wellness.place.model.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kh.wellness.course.model.dto.PlaceDto;
import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.place.model.dao.PlaceMapper;
import com.kh.wellness.place.model.dto.PlaceResponse;
import com.kh.wellness.place.model.dto.PlaceDetailResponse;
import com.kh.wellness.place.model.dto.MapPlaceResponse;
import com.kh.wellness.place.model.vo.MapPlace;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class PlaceService {

	private final PlaceMapper placeMapper;

	public PlaceDetailResponse getPlaceDetail(Long placeNo) {
		PlaceDetailResponse place = placeMapper.selectPlaceDetail(placeNo);
		if (place == null) {
			throw new NotFoundException("존재하지 않는 관광지입니다.");
		}

		place.setPlaceImages(placeMapper.selectPlaceImages(placeNo));
		return place;
	}

	public PlaceDto selectByPlaceNo(Long placeNo) {
		PlaceDto place = placeMapper.selectByPlaceNo(placeNo);
		if(place == null) {
			throw new NotFoundException("존재하지 않는 관광지입니다.");
		}
		return place;
	}
	public List<PlaceResponse> selectPlaces(Long typeDetailNo) {

		List<PlaceResponse> list = placeMapper.selectPlaces(typeDetailNo);
		
		if(list.isEmpty()) {
			throw new NotFoundException("존재하지 않는 관광지입니다.");
		}
		
		return list;
	}

    public List<MapPlaceResponse> findMapPlaces() {
        return toMapPlaceResponses(placeMapper.findMapPlaces());
    }

    public List<MapPlaceResponse> findMapPlacesByType(String type) {
        String normalizedType = requireFilterValue(type, "타입은 필수입니다.");
        return toMapPlaceResponses(placeMapper.findMapPlacesByType(normalizedType));
    }

    public List<MapPlaceResponse> findMapPlacesByTag(String tag) {
        String normalizedTag = requireFilterValue(tag, "태그는 필수입니다.");
        return toMapPlaceResponses(placeMapper.findMapPlacesByTag(normalizedTag));
    }

    private List<MapPlaceResponse> toMapPlaceResponses(List<MapPlace> places) {
        return places.stream()
                .map(this::toMapPlaceResponse)
                .toList();
    }

    private String requireFilterValue(String value, String requiredMessage) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(requiredMessage);
        }
        return value.trim();
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
}
