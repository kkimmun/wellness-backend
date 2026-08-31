package com.kh.wellness.admin.cource.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.wellness.admin.cource.model.dto.AdminCourseListResponse;
import com.kh.wellness.admin.cource.model.dto.AdminCourseRequest;
import com.kh.wellness.admin.cource.model.dto.CourseStatusRequest;
import com.kh.wellness.admin.cource.model.service.AdminCourseService;
import com.kh.wellness.common.api.ApiResponse;
import com.kh.wellness.common.page.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/courses")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCourseController {

    private final AdminCourseService adminCourseService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminCourseListResponse>>> getCourses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String active) {
        PageResponse<AdminCourseListResponse> courses =
                adminCourseService.getCourses(page, keyword, active);
        return ResponseEntity.ok(
                ApiResponse.success("고정 코스 목록 조회 성공", courses));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> saveCourse(
            @Valid @RequestBody AdminCourseRequest request) {
        adminCourseService.saveCourse(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("고정 코스 등록 성공", null));
    }

    @PutMapping("/{courseNo}")
    public ResponseEntity<ApiResponse<Void>> updateCourse(
            @PathVariable Long courseNo,
            @Valid @RequestBody AdminCourseRequest request) {
        adminCourseService.updateCourse(courseNo, request);
        return ResponseEntity.ok(
                ApiResponse.success("고정 코스 수정 성공", null));
    }

    @DeleteMapping("/{courseNo}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(
            @PathVariable Long courseNo) {
        adminCourseService.deleteCourse(courseNo);
        return ResponseEntity.ok(
                ApiResponse.success("고정 코스 삭제 성공", null));
    }

    @PatchMapping("/{courseNo}/status")
    public ResponseEntity<ApiResponse<Void>> updateCourseStatus(
            @PathVariable Long courseNo,
            @Valid @RequestBody CourseStatusRequest request) {
        adminCourseService.updateCourseStatus(courseNo, request.getActive());
        return ResponseEntity.ok(
                ApiResponse.success("고정 코스 상태 변경 성공", null));
    }
}
