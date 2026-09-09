package com.kh.wellness.plan.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.wellness.auth.model.vo.CustomUserDetails;
import com.kh.wellness.common.api.ApiResponse;
import com.kh.wellness.plan.model.dto.PlanDetailResponse;
import com.kh.wellness.plan.model.dto.PlanPlaceRequestDto;
import com.kh.wellness.plan.model.service.PlanService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {
	private final PlanService planService;
	
	@PostMapping
	public ResponseEntity<ApiResponse<Void>> savePlan(@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestBody List<PlanPlaceRequestDto> planRequest) {
		
		Long memberNo = userDetails.getMemberNo();
		
		planService.savePlan(memberNo, planRequest);
	
		return ResponseEntity.status(200).body(ApiResponse.success("요청에 성공하였습니다.", null));
	}
	
	@PutMapping
	public ResponseEntity<ApiResponse<Void>> editPlan(@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestBody List<PlanPlaceRequestDto> planRequest) {
		
		Long memberNo = userDetails.getMemberNo();
		
		planService.editPlan(memberNo, planRequest);
	
		return ResponseEntity.status(200).body(ApiResponse.success("요청에 성공하였습니다.", null));
	}
	
	@DeleteMapping
	public ResponseEntity<ApiResponse<Void>> deletePlan(@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		Long memberNo = userDetails.getMemberNo();
		
		planService.deletePlan(memberNo);
	
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
