package com.kh.wellness.course.model.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.wellness.common.page.PageResponse;
import com.kh.wellness.course.model.dao.CourseMapper;
import com.kh.wellness.course.model.dto.CourseListResponse;
import com.kh.wellness.course.model.dto.CourseListRow;
import com.kh.wellness.exception.BadRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private static final int PAGE_SIZE = 10;

    private final CourseMapper courseMapper;

    public PageResponse<CourseListResponse> getCourses(int page) {
        if (page < 1) {
            throw new BadRequestException("올바르지 않은 조회 조건입니다.");
        }

        long totalElements = courseMapper.countActiveCourses();
        if (totalElements == 0) {
            return PageResponse.empty(page, PAGE_SIZE);
        }

        long offset = (long) (page - 1) * PAGE_SIZE;
        List<CourseListRow> rows = courseMapper.selectActiveCourses(offset, PAGE_SIZE);
        List<CourseListResponse> courses = rows.stream()
                .map(CourseListResponse::from)
                .toList();

        return new PageResponse<>(courses, totalElements, page, PAGE_SIZE);
    }
    
    public Void getCourse(Long courseNo) {
    	
    	return null;
    }
}
