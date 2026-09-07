package com.kh.wellness.place.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.wellness.auth.model.vo.CustomUserDetails;
import com.kh.wellness.common.api.ApiResponse;
import com.kh.wellness.place.model.dto.MapPlaceResponse;
import com.kh.wellness.place.model.dto.PlaceDetailResponse;
import com.kh.wellness.place.model.dto.PlaceResponse;
import com.kh.wellness.place.model.dto.PlaceTagDto;
import com.kh.wellness.place.model.dto.PlaceTypeOptionResponse;
import com.kh.wellness.place.model.service.PlaceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping("/pins")
    public ResponseEntity<ApiResponse<List<MapPlaceResponse>>> findMapPins(
            @RequestParam(name = "typeNo", required = false) Long typeNo,
            @RequestParam(name = "typeDetailNo", required = false) Long typeDetailNo,
            @RequestParam(name = "tagNo", required = false) Long tagNo) {
        List<MapPlaceResponse> places = typeNo == null && typeDetailNo == null && tagNo == null
                ? placeService.findMapPlaces()
                : placeService.findMapPlaces(typeNo, typeDetailNo, tagNo);
        return ResponseEntity.ok(ApiResponse.success("지도 장소 조회 성공", places));
    }

    @GetMapping("/type-options")
    public ResponseEntity<ApiResponse<List<PlaceTypeOptionResponse>>> findPlaceTypeOptions() {
        return ResponseEntity.ok(ApiResponse.success("장소 타입 목록 조회 성공", placeService.findPlaceTypeOptions()));
    }

    @GetMapping("/tag-options")
    public ResponseEntity<ApiResponse<List<PlaceTagDto>>> findPlaceTagOptions() {
        return ResponseEntity.ok(ApiResponse.success("장소 태그 목록 조회 성공", placeService.findPlaceTagOptions()));
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
            @PathVariable(name = "placeNo") Long placeNo,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberNo = userDetails == null ? null : userDetails.getMemberNo();
        PlaceDetailResponse response = placeService.getPlaceDetail(placeNo, memberNo);
        return ResponseEntity.ok(ApiResponse.success("장소 상세 조회 성공", response));
    }

    @GetMapping("/{typeDetailNo}")
    public ResponseEntity<ApiResponse<List<PlaceResponse>>> selectPlaces(
            @PathVariable(name = "typeDetailNo") Long typeDetailNo) {
        return ResponseEntity.ok(ApiResponse.success("조회 성공", placeService.selectPlaces(typeDetailNo)));
    }
}
