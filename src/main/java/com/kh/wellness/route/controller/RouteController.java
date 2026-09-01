package com.kh.wellness.route.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.wellness.common.api.ApiResponse;
import com.kh.wellness.route.model.dto.OriginSearchResponse;
import com.kh.wellness.route.model.dto.RouteResponse;
import com.kh.wellness.route.model.dto.RouteSearchRequest;
import com.kh.wellness.route.model.service.RouteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    @GetMapping
    public ResponseEntity<ApiResponse<RouteResponse>> findRoutes(
            @Valid @ModelAttribute RouteSearchRequest request) {
        RouteResponse response = routeService.findRoutes(request);

        return ResponseEntity.ok(ApiResponse.success("길찾기 조회 성공", response));
    }

    @GetMapping("/origins")
    public ResponseEntity<ApiResponse<List<OriginSearchResponse>>> searchOrigins(
            @RequestParam(name = "query") String query) {
        List<OriginSearchResponse> response = routeService.searchOrigins(query);

        return ResponseEntity.ok(ApiResponse.success("출발지 검색 성공", response));
    }
}
