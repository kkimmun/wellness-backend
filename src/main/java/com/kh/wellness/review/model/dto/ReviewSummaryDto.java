package com.kh.wellness.review.model.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewSummaryDto {

	private Double avgRating;
	private long totalReviewCount;
	private Map<Integer, Integer> ratingDistribution = new LinkedHashMap<>();
}
