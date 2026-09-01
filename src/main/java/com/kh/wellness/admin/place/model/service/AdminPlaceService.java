package com.kh.wellness.admin.place.model.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.wellness.admin.place.model.dao.AdminPlaceMapper;
import com.kh.wellness.admin.place.model.dto.AdminPlaceListResponse;
import com.kh.wellness.common.page.PageResponse;
import com.kh.wellness.exception.BadRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPlaceService {

	private static final int PAGE_SIZE = 10;

	private final AdminPlaceMapper adminPlaceMapper;

	// 관리자 장소 목록 조회 (page 는 1-base 로 받아 내부 0-base 로 변환)
	public PageResponse<AdminPlaceListResponse> getPlaces(int page) {
		validatePage(page);

		long totalElements = adminPlaceMapper.countPlaces();
		
		if (totalElements == 0) {
	        return PageResponse.empty(page, PAGE_SIZE);
	    }

		long offset = (long) (page - 1) * PAGE_SIZE;
		
		List<AdminPlaceListResponse> content = adminPlaceMapper.selectPlaces(offset, PAGE_SIZE);

		return new PageResponse<>(content, totalElements,page, PAGE_SIZE);
	}
	
	 private void validatePage(int page) {
        if (page < 1) {
            throw new BadRequestException("올바르지 않은 조회 조건입니다.");
        }
        
	 }
}
