package com.kh.wellness.plan.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.wellness.auth.model.vo.CustomUserDetails;
import com.kh.wellness.common.api.ApiResponse;
import com.kh.wellness.plan.model.dto.PlanCreateRequestDto;
import com.kh.wellness.plan.model.dto.PlanCreateResponseDto;
import com.kh.wellness.plan.model.dto.PlanDetailResponse;
import com.kh.wellness.plan.model.dto.PlanPlaceRequestDto;
import com.kh.wellness.plan.model.dto.PlanResponseDto;
import com.kh.wellness.plan.model.service.PlanService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {
	private final PlanService planService;

	@PostMapping
	public ResponseEntity<ApiResponse<PlanCreateResponseDto>> createPlan(@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestBody PlanCreateRequestDto request) {

		Long memberNo = userDetails.getMemberNo();

		Long planNo = planService.createPlan(memberNo, request);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.created("플랜 생성에 성공하였습니다.", new PlanCreateResponseDto(planNo)));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<PlanResponseDto>>> findPlans(@AuthenticationPrincipal CustomUserDetails userDetails) {

		Long memberNo = userDetails.getMemberNo();

		List<PlanResponseDto> plans = planService.findPlans(memberNo);

		return ResponseEntity.status(200).body(ApiResponse.success("조회 성공", plans));
	}

	@PostMapping("/{planNo}/places")
	public ResponseEntity<ApiResponse<Void>> addPlaces(@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable Long planNo, @RequestBody List<PlanPlaceRequestDto> planRequest) {

		Long memberNo = userDetails.getMemberNo();

		planService.addPlaces(memberNo, planNo, planRequest);

		return ResponseEntity.status(200).body(ApiResponse.success("요청에 성공하였습니다.", null));
	}

	@PutMapping("/{planNo}/places")
	public ResponseEntity<ApiResponse<Void>> editPlaces(@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable Long planNo, @RequestBody List<PlanPlaceRequestDto> planRequest) {

		Long memberNo = userDetails.getMemberNo();

		planService.editPlaces(memberNo, planNo, planRequest);

		return ResponseEntity.status(200).body(ApiResponse.success("요청에 성공하였습니다.", null));
	}

	@DeleteMapping("/{planNo}")
	public ResponseEntity<ApiResponse<Void>> deletePlan(@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable Long planNo) {

		Long memberNo = userDetails.getMemberNo();

		planService.deletePlan(memberNo, planNo);

		return ResponseEntity.status(200).body(ApiResponse.success("요청에 성공하였습니다.", null));
	}

	@GetMapping("/nearby")
	public ResponseEntity<ApiResponse<List<PlanDetailResponse>>> findNearbyPlaces(@AuthenticationPrincipal CustomUserDetails userDetails,
	        @RequestParam(name = "xAxis") Double xAxis,
	        @RequestParam(name = "yAxis") Double yAxis){

		Long memberNo = userDetails.getMemberNo();

		List<PlanDetailResponse> list = planService.findNearbyPlaces(memberNo, xAxis, yAxis);

		return ResponseEntity.status(200).body(ApiResponse.success("조회 성공", list));
	}

}
