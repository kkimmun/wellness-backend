package com.kh.wellness.bookmark.model.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kh.wellness.bookmark.model.dao.BookmarkMapper;
import com.kh.wellness.bookmark.model.dto.BookmarkStatusResponse;
import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.exception.UnauthorizedException;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

	@Mock
	private BookmarkMapper bookmarkMapper;

	@InjectMocks
	private BookmarkService bookmarkService;

	@Test
	void 북마크한_장소는_조회시_true를_반환한다() {
		when(bookmarkMapper.countActivePlace(1062L)).thenReturn(1);
		when(bookmarkMapper.countBookmark(100L, 1062L)).thenReturn(1);

		BookmarkStatusResponse response = bookmarkService.getBookmarkStatus(100L, 1062L);

		assertThat(response.getPlaceNo()).isEqualTo(1062L);
		assertThat(response.isBookmarked()).isTrue();
	}

	@Test
	void 북마크하지_않은_장소는_조회시_false를_반환한다() {
		when(bookmarkMapper.countActivePlace(1062L)).thenReturn(1);
		when(bookmarkMapper.countBookmark(100L, 1062L)).thenReturn(0);

		BookmarkStatusResponse response = bookmarkService.getBookmarkStatus(100L, 1062L);

		assertThat(response.isBookmarked()).isFalse();
	}

	@Test
	void 로그인하지_않은_상태로_조회하면_401_예외를_발생시킨다() {
		assertThatThrownBy(() -> bookmarkService.getBookmarkStatus(null, 1062L))
				.isInstanceOf(UnauthorizedException.class);
		verifyNoInteractions(bookmarkMapper);
	}

	@Test
	void 존재하지_않는_장소를_조회하면_404_예외를_발생시킨다() {
		when(bookmarkMapper.countActivePlace(9999L)).thenReturn(0);

		assertThatThrownBy(() -> bookmarkService.getBookmarkStatus(100L, 9999L))
				.isInstanceOf(NotFoundException.class);
	}
}
