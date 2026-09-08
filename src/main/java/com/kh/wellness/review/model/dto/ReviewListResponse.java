package com.kh.wellness.review.model.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewListResponse {

	private ReviewSummaryDto summary;
	private List<ReviewItemDto> content;
	private int currentPage;
	private int size;
	private long totalElements;
	private int totalPages;
	private boolean hasNext;
	private boolean hasPrevious;
}
