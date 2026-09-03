package com.kh.wellness.place.model.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kh.wellness.course.model.dto.PlaceDto;
import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.place.model.dao.PlaceMapper;
import com.kh.wellness.place.model.dto.PlaceResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class PlaceService {

	private final PlaceMapper placeMapper;

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
}
