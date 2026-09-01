package com.kh.wellness.admin.cource.model.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kh.wellness.admin.cource.model.dao.AdminCourseMapper;
import com.kh.wellness.admin.cource.model.dto.AdminCourseListResponse;
import com.kh.wellness.admin.cource.model.dto.AdminCourseRequest;
import com.kh.wellness.admin.cource.model.vo.Course;
import com.kh.wellness.admin.cource.model.vo.CourseWaypoint;
import com.kh.wellness.common.page.PageResponse;
import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.exception.ConflictException;
import com.kh.wellness.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class AdminCourseServiceTest {

    @Mock
    private AdminCourseMapper adminCourseMapper;

    private AdminCourseService adminCourseService;

    @BeforeEach
    void setUp() {
        adminCourseService = new AdminCourseService(adminCourseMapper);
    }

    @Test
    void getCoursesAppliesFiltersAndPagination() {
        AdminCourseListResponse row = new AdminCourseListResponse(
                101L, "김포 힐링 코스", 180, "설명", Date.valueOf("2026-08-20"));
        when(adminCourseMapper.countCourses("힐링", "Y")).thenReturn(1L);
        when(adminCourseMapper.selectCourses("힐링", "Y", 10L, 10))
                .thenReturn(List.of(row));

        PageResponse<AdminCourseListResponse> result =
                adminCourseService.getCourses(2, " 힐링 ", "Y");

        assertThat(result.getContent()).containsExactly(row);
        assertThat(result.getCurrentPage()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(adminCourseMapper).selectCourses("힐링", "Y", 10L, 10);
    }

    @Test
    void saveCourseValidatesPlacesAndStoresWaypointSequence() {
        AdminCourseRequest request = request(List.of(5L, 8L, 12L));
        when(adminCourseMapper.countExistingPlaces(anyList())).thenReturn(5);
        when(adminCourseMapper.insertCourse(any(Course.class))).thenAnswer(invocation -> {
            Course course = invocation.getArgument(0);
            course.setCourseNo(101L);
            return 1;
        });
        when(adminCourseMapper.insertCourseWaypoint(any(CourseWaypoint.class)))
                .thenReturn(1);

        adminCourseService.saveCourse(request);

        ArgumentCaptor<CourseWaypoint> waypointCaptor =
                ArgumentCaptor.forClass(CourseWaypoint.class);
        verify(adminCourseMapper, org.mockito.Mockito.times(3))
                .insertCourseWaypoint(waypointCaptor.capture());
        assertThat(waypointCaptor.getAllValues())
                .extracting(CourseWaypoint::getPlaceNo)
                .containsExactly(5L, 8L, 12L);
        assertThat(waypointCaptor.getAllValues())
                .extracting(CourseWaypoint::getWaypointSequence)
                .containsExactly(1, 2, 3);
    }

    @Test
    void saveCourseRejectsUnknownPlace() {
        AdminCourseRequest request = request(List.of(5L));
        when(adminCourseMapper.countExistingPlaces(anyList())).thenReturn(2);

        assertThatThrownBy(() -> adminCourseService.saveCourse(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("선택한 관광지 정보를 찾을 수 없습니다.");

        verify(adminCourseMapper, never()).insertCourse(any(Course.class));
    }

    @Test
    void updateCourseReplacesExistingWaypoints() {
        AdminCourseRequest request = request(List.of(21L, 32L));
        when(adminCourseMapper.countCourseByNo(101L)).thenReturn(1);
        when(adminCourseMapper.countExistingPlaces(anyList())).thenReturn(4);
        when(adminCourseMapper.updateCourse(any(Course.class))).thenReturn(1);
        when(adminCourseMapper.insertCourseWaypoint(any(CourseWaypoint.class)))
                .thenReturn(1);

        adminCourseService.updateCourse(101L, request);

        InOrder order = inOrder(adminCourseMapper);
        order.verify(adminCourseMapper).countCourseByNo(101L);
        order.verify(adminCourseMapper).countExistingPlaces(anyList());
        order.verify(adminCourseMapper).updateCourse(any(Course.class));
        order.verify(adminCourseMapper).deleteCourseWaypoints(101L);
        order.verify(adminCourseMapper, org.mockito.Mockito.times(2))
                .insertCourseWaypoint(any(CourseWaypoint.class));
    }

    @Test
    void updateCourseRejectsDuplicatePlace() {
        AdminCourseRequest request = request(List.of(1L));
        when(adminCourseMapper.countCourseByNo(101L)).thenReturn(1);
        when(adminCourseMapper.countExistingPlaces(anyList())).thenReturn(2);

        assertThatThrownBy(() -> adminCourseService.updateCourse(101L, request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("코스에 동일한 장소를 중복으로 선택할 수 없습니다.");

        verify(adminCourseMapper, never()).updateCourse(any(Course.class));
    }

    @Test
    void deleteCourseDeletesWaypointsBeforeCourse() {
        when(adminCourseMapper.countCourseByNo(101L)).thenReturn(1);
        when(adminCourseMapper.deleteCourse(101L)).thenReturn(1);

        adminCourseService.deleteCourse(101L);

        InOrder order = inOrder(adminCourseMapper);
        order.verify(adminCourseMapper).countCourseByNo(101L);
        order.verify(adminCourseMapper).deleteCourseWaypoints(101L);
        order.verify(adminCourseMapper).deleteCourse(101L);
    }

    @Test
    void updateCourseStatusRejectsValueOtherThanYOrN() {
        assertThatThrownBy(() -> adminCourseService.updateCourseStatus(101L, "ACTIVE"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("올바르지 않은 코스 상태입니다.");

        verify(adminCourseMapper, never()).updateCourseStatus(eq(101L), any());
    }

    private AdminCourseRequest request(List<Long> waypointPlaceNos) {
        return new AdminCourseRequest(
                "김포 힐링 순례길",
                120,
                "전통 사찰과 웰니스 관광지를 둘러보는 코스입니다.",
                1L,
                waypointPlaceNos,
                20L);
    }
}
