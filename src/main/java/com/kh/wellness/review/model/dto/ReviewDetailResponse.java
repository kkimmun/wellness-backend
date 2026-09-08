package com.kh.wellness.review.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDetailResponse {

	private Long reviewNo;
	private Long placeNo;
	private Long memberNo;
	private Integer rating;
	private String reviewContent;
	private String createDate;
	private List<ReviewImageDto> images;
}
