package com.kh.wellness.plan.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.wellness.auth.model.vo.CustomUserDetails;
import com.kh.wellness.common.api.ApiResponse;
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
	

}
