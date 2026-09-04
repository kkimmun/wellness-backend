package com.kh.wellness.place.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.kh.wellness.common.api.ApiResponse;
import com.kh.wellness.place.model.dto.PlaceResponse;
import com.kh.wellness.place.model.dto.PlaceDetailResponse;
import com.kh.wellness.place.model.service.PlaceService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {
	private final PlaceService placeService;

	@GetMapping("/{placeNo}/detail")
	public ResponseEntity<ApiResponse<PlaceDetailResponse>> getPlaceDetail(
			@PathVariable(name = "placeNo") Long placeNo) {
		PlaceDetailResponse place = placeService.getPlaceDetail(placeNo);
		return ResponseEntity.ok(ApiResponse.success("장소 상세 조회 성공", place));
	}
	
	@GetMapping("/{typeDetailNo}")
	public ResponseEntity<ApiResponse<List<PlaceResponse>>> selectPlaces(@PathVariable(name = "typeDetailNo")Long typeDetailNo){
		
		List<PlaceResponse> placeList = placeService.selectPlaces(typeDetailNo);
		
		return ResponseEntity.status(200).body(ApiResponse.success("조회 성공", placeList));
	}

}
