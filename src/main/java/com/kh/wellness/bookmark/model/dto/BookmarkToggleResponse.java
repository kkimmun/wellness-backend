package com.kh.wellness.bookmark.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BookmarkToggleResponse {

	private Long placeNo;
	private boolean bookmarked;
}
