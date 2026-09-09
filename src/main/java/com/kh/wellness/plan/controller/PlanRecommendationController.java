package com.kh.wellness.plan.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.wellness.auth.model.vo.CustomUserDetails;
import com.kh.wellness.common.api.ApiResponse;
import com.kh.wellness.exception.UnauthorizedException;
import com.kh.wellness.plan.model.dto.NearbyPlaceRecommendationResponse;
import com.kh.wellness.plan.model.service.PlanRecommendationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/plan-recommendations")
@RequiredArgsConstructor
public class PlanRecommendationController {

    private final PlanRecommendationService planRecommendationService;

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<NearbyPlaceRecommendationResponse>>> findNearbyPlaces(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "xAxis") Double xAxis,
            @RequestParam(name = "yAxis") Double yAxis,
            @RequestParam(name = "typeNo", required = false) Long typeNo,
            @RequestParam(name = "excludePlaceNos", required = false) List<Long> excludePlaceNos) {

        if (userDetails == null) {
            throw new UnauthorizedException("로그인이 필요한 기능입니다.");
        }

        List<NearbyPlaceRecommendationResponse> result = planRecommendationService.findNearbyPlaces(
                userDetails.getMemberNo(),
                xAxis,
                yAxis,
                typeNo,
                excludePlaceNos);

        return ResponseEntity.ok(ApiResponse.success("주변 추천 장소 조회 성공", result));
    }
}
