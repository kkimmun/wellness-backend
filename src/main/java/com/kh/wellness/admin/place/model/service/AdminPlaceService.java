package com.kh.wellness.admin.place.model.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.wellness.admin.place.model.dao.AdminPlaceMapper;
import com.kh.wellness.admin.place.model.dto.AdminPlaceListResponse;
import com.kh.wellness.common.page.PageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPlaceService {

	private static final int PAGE_SIZE = 10;

	private final AdminPlaceMapper adminPlaceMapper;

	// 관리자 장소 목록 조회 (page 는 1-base 로 받아 내부 0-base 로 변환)
	public PageResponse<AdminPlaceListResponse> getPlaces(int page) {
		int currentPage = page < 1 ? 0 : page - 1;

		long totalElements = adminPlaceMapper.countPlaces();
		if (totalElements == 0) {
			return new PageResponse<>(List.of(), 0, currentPage, PAGE_SIZE);
		}

		int offset = currentPage * PAGE_SIZE;
		List<AdminPlaceListResponse> content = adminPlaceMapper.selectPlaces(offset, PAGE_SIZE);

		return new PageResponse<>(content, totalElements, currentPage, PAGE_SIZE);
	}
}
