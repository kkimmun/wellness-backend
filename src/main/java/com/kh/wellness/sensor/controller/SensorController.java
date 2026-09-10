package com.kh.wellness.sensor.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.wellness.auth.model.vo.CustomUserDetails;
import com.kh.wellness.common.api.ApiResponse;
import com.kh.wellness.sensor.model.dto.SensorRequestDto;
import com.kh.wellness.sensor.model.dto.SensorResponseDto;
import com.kh.wellness.sensor.model.service.SensorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
public class SensorController {
	private final SensorService sensorService;
	
	//화면 단에서 조회용
	@GetMapping	
	public ResponseEntity<ApiResponse<List<SensorResponseDto>>> sensorInfoRequest(@AuthenticationPrincipal CustomUserDetails userDetails){
		Long memberNoFromToken = userDetails.getMemberNo();
		
		List<SensorResponseDto> sensorInfo = sensorService.sensorInfoRequest(memberNoFromToken);
		
		return ResponseEntity.status(200).body(ApiResponse.success("조회성공", sensorInfo));
	}
	
	// 단말에서 받는 정보 DB 저장
	@PostMapping("/response")
	public ResponseEntity<ApiResponse<SensorResponseDto>> insertSensorData(@RequestBody SensorRequestDto sensor){
		
		sensorService.insertSensorData(sensor);
		
		return ResponseEntity.status(201).body(ApiResponse.success("응답성공", null));
	}
}
