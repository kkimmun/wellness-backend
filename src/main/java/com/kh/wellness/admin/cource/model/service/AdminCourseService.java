package com.kh.wellness.admin.cource.model.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.wellness.admin.cource.model.dao.AdminCourseMapper;
import com.kh.wellness.admin.cource.model.dto.AdminCourseListResponse;
import com.kh.wellness.admin.cource.model.dto.AdminCourseRequest;
import com.kh.wellness.admin.cource.model.vo.Course;
import com.kh.wellness.admin.cource.model.vo.CourseWaypoint;
import com.kh.wellness.common.page.PageResponse;
import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.exception.ConflictException;
import com.kh.wellness.exception.InternalServerException;
import com.kh.wellness.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCourseService {

    private static final int PAGE_SIZE = 10;

    private final AdminCourseMapper adminCourseMapper;

    public PageResponse<AdminCourseListResponse> getCourses(
            int page, String keyword, String active) {
        validatePage(page);
        String normalizedKeyword = normalizeKeyword(keyword);
        String normalizedActive = normalizeActiveFilter(active);
        long totalElements = adminCourseMapper.countCourses(
                normalizedKeyword, normalizedActive);

        if (totalElements == 0) {
            return PageResponse.empty(page, PAGE_SIZE);
        }

        long offset = (long) (page - 1) * PAGE_SIZE;
        List<AdminCourseListResponse> courses = adminCourseMapper.selectCourses(
                normalizedKeyword, normalizedActive, offset, PAGE_SIZE);

        return new PageResponse<>(courses, totalElements, page, PAGE_SIZE);
    }

    @Transactional
    public void saveCourse(AdminCourseRequest request) {
        validatePlaces(request);
        validateDuplicatePlace(request.getStartPlaceNo(),
        					   request.getWaypointPlaceNos(),
        					   request.getEndPlaceNo());
        Course course = toCourse(request, null);
        int result = adminCourseMapper.insertCourse(course);
        if (result != 1 || course.getCourseNo() == null) {
            throw new InternalServerException("고정 코스 등록 중 오류가 발생했습니다.");
        }
        
        

        saveWaypoints(course.getCourseNo(), request.getWaypointPlaceNos(),
                "고정 코스 등록 중 오류가 발생했습니다.");
    }

    @Transactional
    public void updateCourse(Long courseNo, AdminCourseRequest request) {
        validateCourseNo(courseNo);
        validateCourseExists(courseNo);
        validatePlaces(request);
        validateDuplicatePlace(request.getStartPlaceNo(),
                               request.getWaypointPlaceNos(),
                               request.getEndPlaceNo());

        int result = adminCourseMapper.updateCourse(toCourse(request, courseNo));
        if (result != 1) {
            throw new InternalServerException("고정 코스 수정 중 오류가 발생했습니다.");
        }

        adminCourseMapper.deleteCourseWaypoints(courseNo);
        saveWaypoints(courseNo, request.getWaypointPlaceNos(),
                "고정 코스 수정 중 오류가 발생했습니다.");
    }

    @Transactional
    public void deleteCourse(Long courseNo) {
        validateCourseNo(courseNo);
        validateCourseExists(courseNo);

        adminCourseMapper.deleteCourseWaypoints(courseNo);
        int result = adminCourseMapper.deleteCourse(courseNo);
        if (result != 1) {
            throw new InternalServerException("고정 코스 삭제 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public void updateCourseStatus(Long courseNo, String active) {
        validateCourseNo(courseNo);
        if (active == null || active.isBlank()) {
            throw new BadRequestException("올바르지 않은 코스 상태입니다.");
        }
        String normalizedActive = active.trim();
        if (!"Y".equals(normalizedActive) && !"N".equals(normalizedActive)) {
            throw new BadRequestException("올바르지 않은 코스 상태입니다.");
        }
        validateCourseExists(courseNo);

        int result = adminCourseMapper.updateCourseStatus(courseNo, normalizedActive);
        if (result != 1) {
            throw new InternalServerException("고정 코스 상태 변경 중 오류가 발생했습니다.");
        }
    }

    private void validatePage(int page) {
        if (page < 1) {
            throw new BadRequestException("올바르지 않은 조회 조건입니다.");
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    private String normalizeActiveFilter(String active) {
        if (active == null || active.isBlank()) {
            return null;
        }
        String normalized = active.trim();
        if (!"Y".equals(normalized) && !"N".equals(normalized)) {
            throw new BadRequestException("올바르지 않은 조회 조건입니다.");
        }
        return normalized;
    }

    private void validateCourseNo(Long courseNo) {
        if (courseNo == null || courseNo <= 0) {
            throw new BadRequestException("올바르지 않은 고정 코스 번호입니다.");
        }
    }

    private void validateCourseExists(Long courseNo) {
        if (adminCourseMapper.countCourseByNo(courseNo) == 0) {
            throw new NotFoundException("고정 코스를 찾을 수 없습니다.");
        }
    }

    private void validatePlaces(AdminCourseRequest request) {
        Set<Long> placeNos = new LinkedHashSet<>();
        placeNos.add(request.getStartPlaceNo());
        if (request.getWaypointPlaceNos() != null) {
            placeNos.addAll(request.getWaypointPlaceNos());
        }
        placeNos.add(request.getEndPlaceNo());

        List<Long> distinctPlaceNos = new ArrayList<>(placeNos);
        int existingCount = adminCourseMapper.countExistingPlaces(distinctPlaceNos);
        if (existingCount != distinctPlaceNos.size()) {
            throw new NotFoundException("선택한 관광지 정보를 찾을 수 없습니다.");
        }
    }

    private Course toCourse(AdminCourseRequest request, Long courseNo) {
        return Course.builder()
                .courseNo(courseNo)
                .startPlace(request.getStartPlaceNo())
                .endPlace(request.getEndPlaceNo())
                .courseName(request.getCourseName().trim())
                .estimatedTime(request.getEstimatedTime())
                .description(request.getDescription().trim())
                .build();
    }

    private void saveWaypoints(
            Long courseNo, List<Long> waypointPlaceNos, String errorMessage) {
        if (waypointPlaceNos == null || waypointPlaceNos.isEmpty()) {
            return;
        }

        for (int index = 0; index < waypointPlaceNos.size(); index++) {
            CourseWaypoint waypoint = CourseWaypoint.builder()
                    .courseNo(courseNo)
                    .placeNo(waypointPlaceNos.get(index))
                    .waypointSequence(index + 1)
                    .build();
            if (adminCourseMapper.insertCourseWaypoint(waypoint) != 1) {
                throw new InternalServerException(errorMessage);
            }
        }
    }
    
    private void validateDuplicatePlace(
            Long startPlaceNo,
            List<Long> waypointPlaceNos,
            Long endPlaceNo
    ) {
        Set<Long> placeNos = new HashSet<>();

        if (!placeNos.add(startPlaceNo)) {
            throw new ConflictException("코스에 동일한 장소를 중복으로 선택할 수 없습니다.");
        }

        if (waypointPlaceNos != null) {
            for (Long waypointPlaceNo : waypointPlaceNos) {
                if (!placeNos.add(waypointPlaceNo)) {
                    throw new ConflictException("코스에 동일한 장소를 중복으로 선택할 수 없습니다.");
                }
            }
        }

        if (!placeNos.add(endPlaceNo)) {
            throw new ConflictException("코스에 동일한 장소를 중복으로 선택할 수 없습니다.");
        }
    }
}
