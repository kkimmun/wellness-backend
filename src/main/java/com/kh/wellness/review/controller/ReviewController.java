package com.kh.wellness.review.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.wellness.auth.model.vo.CustomUserDetails;
import com.kh.wellness.common.api.ApiResponse;
import com.kh.wellness.review.model.dto.ReviewCreateRequest;
import com.kh.wellness.review.model.dto.ReviewCreateResponse;
import com.kh.wellness.review.model.dto.ReviewDetailResponse;
import com.kh.wellness.review.model.dto.ReviewListResponse;
import com.kh.wellness.review.model.dto.ReviewUpdateRequest;
import com.kh.wellness.review.model.dto.ReviewUpdateResponse;
import com.kh.wellness.review.model.service.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/places/{placeNo}/reviews")
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;

	@PostMapping
	public ResponseEntity<ApiResponse<ReviewCreateResponse>> createReview(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable(name = "placeNo") Long placeNo,
			@Valid @ModelAttribute ReviewCreateRequest request) {

		ReviewCreateResponse data = reviewService.createReview(memberNo(userDetails), placeNo, request);

		return ResponseEntity.status(201).body(ApiResponse.created("리뷰 등록 성공", data));
	}

	@GetMapping("/{reviewNo}")
	public ResponseEntity<ApiResponse<ReviewDetailResponse>> getReview(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable(name = "placeNo") Long placeNo,
			@PathVariable(name = "reviewNo") Long reviewNo) {

		ReviewDetailResponse data = reviewService.getReviewForEdit(memberNo(userDetails), placeNo, reviewNo);

		return ResponseEntity.status(200).body(ApiResponse.success("리뷰 조회 성공", data));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<ReviewListResponse>> getReviews(
			@PathVariable(name = "placeNo") Long placeNo,
			@RequestParam(name = "page", defaultValue = "1") int page) {

		ReviewListResponse data = reviewService.getReviews(placeNo, page);

		return ResponseEntity.status(200).body(ApiResponse.success("요청에 성공하였습니다.", data));
	}

	@PatchMapping("/{reviewNo}")
	public ResponseEntity<ApiResponse<ReviewUpdateResponse>> updateReview(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable(name = "placeNo") Long placeNo,
			@PathVariable(name = "reviewNo") Long reviewNo,
			@Valid @ModelAttribute ReviewUpdateRequest request) {

		ReviewUpdateResponse data = reviewService.updateReview(memberNo(userDetails), placeNo, reviewNo, request);

		return ResponseEntity.status(200).body(ApiResponse.success("리뷰 수정 성공", data));
	}

	@DeleteMapping("/{reviewNo}")
	public ResponseEntity<ApiResponse<Void>> deleteReview(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable(name = "placeNo") Long placeNo,
			@PathVariable(name = "reviewNo") Long reviewNo) {

		reviewService.deleteReview(memberNo(userDetails), placeNo, reviewNo);

		return ResponseEntity.status(200).body(ApiResponse.success("리뷰 삭제 성공", null));
	}

	private Long memberNo(CustomUserDetails userDetails) {
		return userDetails == null ? null : userDetails.getMemberNo();
	}
}
