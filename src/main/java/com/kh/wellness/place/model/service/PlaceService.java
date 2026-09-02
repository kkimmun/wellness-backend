package com.kh.wellness.place.model.service;

import org.springframework.stereotype.Service;

import com.kh.wellness.admin.place.model.vo.Place;
import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.place.model.dao.PlaceMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlaceService {
	
	private final PlaceMapper placeMapper;

	public Place selectByPlace(Long PlaceNo) {
		Place place = placeMapper.selectByPlaceNo(PlaceNo);
		if(place == null) {
			throw new NotFoundException("존재하지 않는 장소입니다.");
		}
		return place;
	}
	
	
	
	

}
