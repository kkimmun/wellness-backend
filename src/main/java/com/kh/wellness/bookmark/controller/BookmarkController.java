package com.kh.wellness.bookmark.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.wellness.auth.model.vo.CustomUserDetails;
import com.kh.wellness.bookmark.model.dto.BookmarkToggleResponse;
import com.kh.wellness.bookmark.model.service.BookmarkService;
import com.kh.wellness.common.api.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/places/{placeNo}/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

	private final BookmarkService bookmarkService;

	@PostMapping
	public ResponseEntity<ApiResponse<BookmarkToggleResponse>> toggleBookmark(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable(name = "placeNo") Long placeNo) {

		Long memberNo = userDetails == null ? null : userDetails.getMemberNo();
		BookmarkToggleResponse data = bookmarkService.toggleBookmark(memberNo, placeNo);

		String message = data.isBookmarked() ? "북마크 등록 성공" : "북마크 취소 성공";
		return ResponseEntity.status(200).body(ApiResponse.success(message, data));
	}
}
