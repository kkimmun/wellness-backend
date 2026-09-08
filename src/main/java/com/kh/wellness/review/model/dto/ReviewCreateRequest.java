package com.kh.wellness.review.model.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateRequest {

	@NotNull(message = "평점을 입력해주세요.")
	@Min(value = 1, message = "평점은 1점부터 5점까지 입력할 수 있습니다.")
	@Max(value = 5, message = "평점은 1점부터 5점까지 입력할 수 있습니다.")
	private Integer rating;

	// 리뷰 내용은 선택 (REVIEW_CONTENT nullable)
	private String reviewContent;

	// 이미지는 선택, 1장만 등록한다.
	private MultipartFile image;
}
