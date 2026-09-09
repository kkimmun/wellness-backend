package com.kh.wellness.recommendation.course.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.wellness.auth.model.vo.CustomUserDetails;
import com.kh.wellness.common.api.ApiResponse;
import com.kh.wellness.exception.UnauthorizedException;
import com.kh.wellness.recommendation.course.model.dto.CourseRecommendationRequest;
import com.kh.wellness.recommendation.course.model.dto.CourseRecommendationResponse;
import com.kh.wellness.recommendation.course.model.service.CourseRecommendationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/course-recommendations")
@RequiredArgsConstructor
public class CourseRecommendationController {

    private final CourseRecommendationService courseRecommendationService;

    @PostMapping
    public ResponseEntity<ApiResponse<CourseRecommendationResponse>> recommendCourse(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CourseRecommendationRequest request) {
        if (userDetails == null) {
            throw new UnauthorizedException("로그인이 필요한 기능입니다.");
        }

        CourseRecommendationResponse response = courseRecommendationService.recommend(
                userDetails.getMemberNo(),
                request);
        return ResponseEntity.ok(ApiResponse.success("추천 코스 생성 성공", response));
    }
}
