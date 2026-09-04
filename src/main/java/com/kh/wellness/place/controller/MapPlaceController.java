package com.kh.wellness.place.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.wellness.common.api.ApiResponse;
import com.kh.wellness.place.model.dto.MapPlaceResponse;
import com.kh.wellness.place.model.service.PlaceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
public class MapPlaceController {

    private final PlaceService placeService;

    @GetMapping("/pins")
    public ResponseEntity<ApiResponse<List<MapPlaceResponse>>> findMapPins() {
        List<MapPlaceResponse> response = placeService.findMapPlaces();

        return ResponseEntity.ok(ApiResponse.success("지도 장소 조회 성공", response));
    }

    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<MapPlaceResponse>>> findMapPinsByType(
            @RequestParam(name = "type", required = false) String type) {
        List<MapPlaceResponse> response = placeService.findMapPlacesByType(type);

        return ResponseEntity.ok(ApiResponse.success("타입별 장소 조회 성공", response));
    }

    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<MapPlaceResponse>>> findMapPinsByTag(
            @RequestParam(name = "tag", required = false) String tag) {
        List<MapPlaceResponse> response = placeService.findMapPlacesByTag(tag);

        return ResponseEntity.ok(ApiResponse.success("태그별 장소 조회 성공", response));
    }
}
