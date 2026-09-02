package com.kh.wellness.admin.place.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kh.wellness.admin.place.model.dto.AdminPlaceCreateRequest;
import com.kh.wellness.admin.place.model.dto.AdminPlaceDetailResponse;
import com.kh.wellness.admin.place.model.dto.AdminPlaceListResponse;
import com.kh.wellness.admin.place.model.dto.AdminPlaceUpdateRequest;
import com.kh.wellness.admin.place.model.dto.PlaceNosRequest;
import com.kh.wellness.admin.place.model.service.AdminPlaceService;
import com.kh.wellness.common.api.ApiResponse;
import com.kh.wellness.common.page.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

	@GetMapping("/{placeNo}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<AdminPlaceDetailResponse>> getPlace(@PathVariable(name = "placeNo") Long placeNo) {

		AdminPlaceDetailResponse data = adminPlaceService.getPlace(placeNo);

		return ResponseEntity.status(200).body(ApiResponse.success("조회 성공", data));
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> savePlace(
			@Valid @ModelAttribute AdminPlaceCreateRequest request,
			@RequestParam(name = "imageFiles", required = false) List<MultipartFile> imageFiles) {

		request.setImageFiles(imageFiles);
		adminPlaceService.savePlace(request);

		return ResponseEntity.status(201).body(ApiResponse.created("생성 요청에 성공하였습니다.", null));
	}

	@PatchMapping("/{placeNo}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> updatePlace(
			@PathVariable(name = "placeNo") Long placeNo,
			@Valid @ModelAttribute AdminPlaceUpdateRequest request,
			@RequestParam(name = "deleteImgNos", required = false) List<Long> deleteImgNos,
			@RequestParam(name = "imageFiles", required = false) List<MultipartFile> imageFiles) {

		adminPlaceService.updatePlace(placeNo, request, deleteImgNos, imageFiles);

		return ResponseEntity.status(200).body(ApiResponse.success("게시글 수정 완료", null));
	}

	@DeleteMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> deletePlaces(@Valid @RequestBody PlaceNosRequest request) {

		int count = adminPlaceService.deletePlaces(request.getPlaceNos());

		return ResponseEntity.status(200).body(ApiResponse.success(count + "건 데이터 삭제 성공", null));
	}

	@PatchMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> restorePlaces(@Valid @RequestBody PlaceNosRequest request) {

		int count = adminPlaceService.restorePlaces(request.getPlaceNos());

		return ResponseEntity.status(200).body(ApiResponse.success(count + "건 데이터 복구 성공", null));
	}
}
