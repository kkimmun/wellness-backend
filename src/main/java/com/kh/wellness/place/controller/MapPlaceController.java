package com.kh.wellness.place.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.wellness.common.api.ApiResponse;
import com.kh.wellness.place.model.dto.MapPlaceResponse;
import com.kh.wellness.place.model.dto.PlaceDetailResponse;
import com.kh.wellness.place.model.dto.PlaceResponse;
import com.kh.wellness.place.model.service.PlaceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
public class MapPlaceController {

    private final PlaceService placeService;

    @GetMapping("/pins")
    public ResponseEntity<ApiResponse<List<MapPlaceResponse>>> findMapPins() {
        return ResponseEntity.ok(ApiResponse.success("지도 장소 조회 성공", placeService.findMapPlaces()));
    }

    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<MapPlaceResponse>>> findMapPinsByType(
            @RequestParam(name = "type", required = false) String type) {
        return ResponseEntity.ok(ApiResponse.success("타입별 장소 조회 성공", placeService.findMapPlacesByType(type)));
    }

    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<MapPlaceResponse>>> findMapPinsByTag(
            @RequestParam(name = "tag", required = false) String tag) {
        return ResponseEntity.ok(ApiResponse.success("태그별 장소 조회 성공", placeService.findMapPlacesByTag(tag)));
    }

    @GetMapping("/{placeNo}/detail")
    public ResponseEntity<ApiResponse<PlaceDetailResponse>> getPlaceDetail(
            @PathVariable(name = "placeNo") Long placeNo) {
        return ResponseEntity.ok(ApiResponse.success("장소 상세 조회 성공", placeService.getPlaceDetail(placeNo)));
    }

    @GetMapping("/{typeDetailNo}")
    public ResponseEntity<ApiResponse<List<PlaceResponse>>> selectPlaces(
            @PathVariable(name = "typeDetailNo") Long typeDetailNo) {
        return ResponseEntity.ok(ApiResponse.success("조회 성공", placeService.selectPlaces(typeDetailNo)));
    }
}
