package com.kh.wellness.bookmark.model.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.wellness.bookmark.model.dao.BookmarkMapper;
import com.kh.wellness.bookmark.model.dto.BookmarkToggleResponse;
import com.kh.wellness.bookmark.model.vo.PlaceBookmark;
import com.kh.wellness.exception.InternalServerException;
import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookmarkService {

	private final BookmarkMapper bookmarkMapper;

	/**
	 * 현재 회원의 장소 북마크를 토글한다.
	 * 이미 북마크한 장소면 취소하고, 아니면 등록한다.
	 */
	@Transactional
	public BookmarkToggleResponse toggleBookmark(Long memberNo, Long placeNo) {
		requireLogin(memberNo);
		requireActivePlace(placeNo);

		boolean bookmarked;
		if (bookmarkMapper.countBookmark(memberNo, placeNo) > 0) {
			if (bookmarkMapper.deleteBookmark(memberNo, placeNo) != 1) {
				throw new InternalServerException("북마크 취소에 실패했습니다.");
			}
			bookmarked = false;
		} else {
			PlaceBookmark bookmark = PlaceBookmark.builder()
					.memberNo(memberNo)
					.placeNo(placeNo)
					.build();

			if (bookmarkMapper.insertBookmark(bookmark) != 1) {
				throw new InternalServerException("북마크 등록에 실패했습니다.");
			}
			bookmarked = true;
		}

		return BookmarkToggleResponse.builder()
				.placeNo(placeNo)
				.bookmarked(bookmarked)
				.build();
	}

	private void requireLogin(Long memberNo) {
		if (memberNo == null) {
			throw new UnauthorizedException("로그인이 필요한 서비스입니다.");
		}
	}

	private void requireActivePlace(Long placeNo) {
		if (bookmarkMapper.countActivePlace(placeNo) == 0) {
			throw new NotFoundException("잘못된 접근입니다.");
		}
	}
}
