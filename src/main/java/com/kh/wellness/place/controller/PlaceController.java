package com.kh.wellness.place.controller;

import com.kh.wellness.auth.model.vo.CustomUserDetails;
import com.kh.wellness.common.api.ApiResponse;
import com.kh.wellness.place.model.dto.PlaceDetailResponse;
import com.kh.wellness.place.model.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/place")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping("/{placeNo}")
    public ResponseEntity<ApiResponse<PlaceDetailResponse>> getPlaceDetail(
            @PathVariable("placeNo") Long placeNo,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long memberNo = (userDetails != null) ? userDetails.getMemberNo() : null;
        PlaceDetailResponse response = placeService.getPlaceDetail(placeNo, memberNo);
        
        return ResponseEntity.ok(ApiResponse.success("요청에 성공하였습니다.", response));
    }
}
