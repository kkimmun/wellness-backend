package com.kh.wellness.course.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.wellness.common.api.ApiResponse;
import com.kh.wellness.common.page.PageResponse;
import com.kh.wellness.course.model.dto.CourseListResponse;
import com.kh.wellness.course.model.dto.CourseResponse;
import com.kh.wellness.course.model.dto.CustomCourseRequest;
import com.kh.wellness.course.model.dto.WaypointsRequest;
import com.kh.wellness.course.model.service.CourseService;
import com.kh.wellness.route.model.dto.PlaceCandidate;

import jakarta.validation.Valid;
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
    
    @PostMapping("/waypoints")
    public ResponseEntity<ApiResponse<List<PlaceCandidate>>> getWaypoints(@Valid @RequestBody WaypointsRequest request) {
    	return ResponseEntity.ok(ApiResponse.success("중간 코스 추천 성공", courseService.getWaypoints(request)));
    }
    
    @PostMapping("/descriptions")
    public ResponseEntity<ApiResponse<CourseResponse>> getCustomCourse(@Valid @RequestBody CustomCourseRequest request) { 
    	return ResponseEntity.ok(ApiResponse.success("코스 안내 성공", courseService.getCustomCourse(request)));
    }
}
