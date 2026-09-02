package com.kh.wellness.route.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.wellness.common.api.ApiResponse;
import com.kh.wellness.route.model.dto.MapPlaceResponse;
import com.kh.wellness.route.model.service.RouteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
public class MapPlaceController {

    private final RouteService routeService;

    @GetMapping("/pins")
    public ResponseEntity<ApiResponse<List<MapPlaceResponse>>> findMapPins() {
        List<MapPlaceResponse> response = routeService.findMapPlaces();

        return ResponseEntity.ok(ApiResponse.success("지도 장소 조회 성공", response));
    }
}
