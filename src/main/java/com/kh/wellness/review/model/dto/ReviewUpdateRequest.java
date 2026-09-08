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
public class ReviewUpdateRequest {

	@NotNull(message = "평점을 입력해주세요.")
	@Min(value = 1, message = "평점은 1점부터 5점까지 입력할 수 있습니다.")
	@Max(value = 5, message = "평점은 1점부터 5점까지 입력할 수 있습니다.")
	private Integer rating;

	private String reviewContent;

	// image 파트를 함께 보내면 리뷰 이미지를 이 이미지로 교체한다. (1장만)
	// 파트를 생략하면 기존 이미지를 유지하고, 빈 파트를 보내면 기존 이미지를 삭제한다.
	private MultipartFile image;
}
