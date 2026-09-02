package com.kh.wellness.admin.place.model.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kh.wellness.admin.place.model.dao.AdminPlaceMapper;
import com.kh.wellness.admin.place.model.dto.AdminPlaceListResponse;
import com.kh.wellness.common.page.PageResponse;

@ExtendWith(MockitoExtension.class)
class AdminPlaceServiceTest {

	@Mock
	private AdminPlaceMapper adminPlaceMapper;

	@InjectMocks
	private AdminPlaceService adminPlaceService;

	@Test
	@DisplayName("장소가 존재하면 조회 결과와 페이징 정보를 담아 반환한다")
	void getPlaces_returnsPagedContent() {
		when(adminPlaceMapper.countPlaces()).thenReturn(1L);
		when(adminPlaceMapper.selectPlaces(0, 10))
				.thenReturn(List.of(new AdminPlaceListResponse()));

		PageResponse<AdminPlaceListResponse> result = adminPlaceService.getPlaces(1);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getCurrentPage()).isZero();
		assertThat(result.getSize()).isEqualTo(10);
	}

	@Test
	@DisplayName("조회 건수가 없으면 빈 목록을 반환하고 목록 조회 쿼리를 호출하지 않는다")
	void getPlaces_empty() {
		when(adminPlaceMapper.countPlaces()).thenReturn(0L);

		PageResponse<AdminPlaceListResponse> result = adminPlaceService.getPlaces(1);

		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalElements()).isZero();
		assertThat(result.getTotalPages()).isZero();
		assertThat(result.getCurrentPage()).isZero();
		verify(adminPlaceMapper, never()).selectPlaces(anyInt(), anyInt());
	}
 
	@Test
	@DisplayName("page 파라미터를 1-base 로 받아 0-base offset 으로 변환한다")
	void getPlaces_convertsPageToZeroBasedOffset() {
		when(adminPlaceMapper.countPlaces()).thenReturn(25L);
		when(adminPlaceMapper.selectPlaces(10, 10))
				.thenReturn(List.of(new AdminPlaceListResponse()));

		PageResponse<AdminPlaceListResponse> result = adminPlaceService.getPlaces(2);

		assertThat(result.getCurrentPage()).isEqualTo(1);
		assertThat(result.getTotalPages()).isEqualTo(3);
		verify(adminPlaceMapper).selectPlaces(10, 10);
	}
}
