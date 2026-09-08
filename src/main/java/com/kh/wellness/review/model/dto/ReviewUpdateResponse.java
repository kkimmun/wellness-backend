package com.kh.wellness.review.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReviewUpdateResponse {

	private Long reviewNo;
	private String updateDate;
}
