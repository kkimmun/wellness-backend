package com.kh.wellness.admin.place.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.wellness.admin.place.model.dto.AdminPlaceListResponse;
import com.kh.wellness.admin.place.model.service.AdminPlaceService;
import com.kh.wellness.common.api.ApiResponse;
import com.kh.wellness.common.page.PageResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/places")
@RequiredArgsConstructor
public class AdminPlaceController {

	private final AdminPlaceService adminPlaceService;

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<PageResponse<AdminPlaceListResponse>>> getPlaces(
			@RequestParam(name = "page", defaultValue = "1") int page) {

		PageResponse<AdminPlaceListResponse> data = adminPlaceService.getPlaces(page);

		return ResponseEntity.status(200).body(ApiResponse.success("요청에 성공하였습니다.", data));
	}
}
