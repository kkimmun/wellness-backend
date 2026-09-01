package com.kh.wellness.course.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.wellness.common.api.ApiResponse;
import com.kh.wellness.common.page.PageResponse;
import com.kh.wellness.course.model.dto.CourseListResponse;
import com.kh.wellness.course.model.dto.CourseResponse;
import com.kh.wellness.course.model.service.CourseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CourseListResponse>>> getCourses(
            @RequestParam(name="page", defaultValue = "1") int page) {
        return ResponseEntity.ok(
                ApiResponse.success("고정 코스 목록 조회 성공", courseService.getCourses(page)));
    }
    
    @GetMapping("/{courseNo}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourse(@PathVariable(name="courseNo") Long courseNo){
    	return ResponseEntity.ok(ApiResponse.success("고정 코스 상세 조회 성공", courseService.getCourse(courseNo)));
    }
}
