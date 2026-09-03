package com.kh.wellness.admin.course.model.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.kh.wellness.admin.course.model.dao.AdminCourseMapper;
import com.kh.wellness.admin.course.model.dto.AdminCourseListResponse;
import com.kh.wellness.admin.course.model.dto.AdminCourseDetailResponse;
import com.kh.wellness.admin.course.model.dto.AdminCourseRequest;
import com.kh.wellness.admin.course.model.service.AdminCourseService;
import com.kh.wellness.admin.course.model.vo.Course;
import com.kh.wellness.admin.course.model.vo.CourseWaypoint;
import com.kh.wellness.common.page.PageResponse;
import com.kh.wellness.course.model.service.CourseService;
import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.exception.ConflictException;
import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.route.model.dto.PlaceResponse;
import com.kh.wellness.route.model.dto.RouteResponse;
import com.kh.wellness.route.model.dto.RouteSearchRequest;

@ExtendWith(MockitoExtension.class)
class AdminCourseServiceTest {

    @Mock
    private AdminCourseMapper adminCourseMapper;

    @Mock
    private CourseService courseService;

    private AdminCourseService adminCourseService;

    @BeforeEach
    void setUp() {
        adminCourseService = new AdminCourseService(adminCourseMapper, courseService);
    }

    @Test
    void getCoursesAppliesFiltersAndPagination() {
        AdminCourseListResponse row = new AdminCourseListResponse(
                101L, "김포 힐링 코스", "설명", Date.valueOf("2026-08-20"), "Y");
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
    void saveCourseStoresShortestWaypointOrderAndKeepsEndpoints() {
        AdminCourseRequest request = request(List.of(5L, 8L, 12L));

        when(adminCourseMapper.countExistingPlaces(anyList()))
                .thenReturn(5);
        when(courseService.getRecommendedRoute(any(RouteSearchRequest.class)))
                .thenReturn(routeResponse(List.of(12L, 5L, 8L)));

        when(adminCourseMapper.insertCourse(any(Course.class)))
                .thenAnswer(invocation -> {
                    Course course = invocation.getArgument(0);

                    // DB의 generated key가 설정되는 상황을 테스트에서 재현
                    ReflectionTestUtils.setField(course, "courseNo", 101L);

                    return 1;
                });

        when(adminCourseMapper.insertCourseWaypoint(any(CourseWaypoint.class)))
                .thenReturn(1);

        adminCourseService.saveCourse(request);

        ArgumentCaptor<CourseWaypoint> waypointCaptor =
                ArgumentCaptor.forClass(CourseWaypoint.class);

        verify(adminCourseMapper, times(3))
                .insertCourseWaypoint(waypointCaptor.capture());

        assertThat(waypointCaptor.getAllValues())
                .extracting(CourseWaypoint::getPlaceNo)
                .containsExactly(12L, 5L, 8L);

        assertThat(waypointCaptor.getAllValues())
                .extracting(CourseWaypoint::getWaypointSequence)
                .containsExactly(1, 2, 3);

        assertThat(waypointCaptor.getAllValues())
                .extracting(CourseWaypoint::getCourseNo)
                .containsOnly(101L);
        assertThat(request.getWaypointPlaceNos()).containsExactly(5L, 8L, 12L);

        ArgumentCaptor<RouteSearchRequest> routeCaptor =
                ArgumentCaptor.forClass(RouteSearchRequest.class);
        verify(courseService).getRecommendedRoute(routeCaptor.capture());
        RouteSearchRequest routeRequest = routeCaptor.getValue();
        assertThat(routeRequest.getStartPlaceNo()).isEqualTo(1L);
        assertThat(routeRequest.getEndPlaceNo()).isEqualTo(20L);
        assertThat(routeRequest.getWaypointPlaceNos()).containsExactly(5L, 8L, 12L);
        assertThat(routeRequest.getTransportType()).isEqualTo("WALK");
        assertThat(routeRequest.getRouteOption()).isEqualTo("SHORTEST");

        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(adminCourseMapper).insertCourse(courseCaptor.capture());
        assertThat(courseCaptor.getValue().getStartPlace()).isEqualTo(1L);
        assertThat(courseCaptor.getValue().getEndPlace()).isEqualTo(20L);
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
    void updateCourseReplacesExistingWaypointsWithShortestOrder() {
        AdminCourseRequest request = request(List.of(21L, 32L));
        when(adminCourseMapper.countCourseByNo(101L)).thenReturn(1);
        when(adminCourseMapper.countExistingPlaces(anyList())).thenReturn(4);
        when(courseService.getRecommendedRoute(any(RouteSearchRequest.class)))
                .thenReturn(routeResponse(List.of(32L, 21L)));
        when(adminCourseMapper.updateCourse(any(Course.class))).thenReturn(1);
        when(adminCourseMapper.insertCourseWaypoint(any(CourseWaypoint.class)))
                .thenReturn(1);

        adminCourseService.updateCourse(101L, request);

        InOrder order = inOrder(adminCourseMapper, courseService);
        order.verify(adminCourseMapper).countCourseByNo(101L);
        order.verify(adminCourseMapper).countExistingPlaces(anyList());
        order.verify(courseService).getRecommendedRoute(any(RouteSearchRequest.class));
        order.verify(adminCourseMapper).updateCourse(any(Course.class));
        order.verify(adminCourseMapper).deleteCourseWaypoints(101L);
        ArgumentCaptor<CourseWaypoint> waypointCaptor =
                ArgumentCaptor.forClass(CourseWaypoint.class);
        order.verify(adminCourseMapper, times(2))
                .insertCourseWaypoint(waypointCaptor.capture());
        assertThat(waypointCaptor.getAllValues())
                .extracting(CourseWaypoint::getPlaceNo)
                .containsExactly(32L, 21L);
        assertThat(waypointCaptor.getAllValues())
                .extracting(CourseWaypoint::getWaypointSequence)
                .containsExactly(1, 2);
    }

    @ParameterizedTest
    @MethodSource("waypointsWithoutOrdering")
    void saveCourseSkipsRouteLookupWhenOrderCannotChange(List<Long> waypointPlaceNos) {
        int waypointCount = waypointPlaceNos == null ? 0 : waypointPlaceNos.size();
        when(adminCourseMapper.countExistingPlaces(anyList())).thenReturn(waypointCount + 2);
        when(adminCourseMapper.insertCourse(any(Course.class)))
                .thenAnswer(invocation -> {
                    Course course = invocation.getArgument(0);
                    ReflectionTestUtils.setField(course, "courseNo", 101L);
                    return 1;
                });
        if (waypointCount == 1) {
            when(adminCourseMapper.insertCourseWaypoint(any(CourseWaypoint.class))).thenReturn(1);
        }

        adminCourseService.saveCourse(request(waypointPlaceNos));

        verifyNoInteractions(courseService);
        ArgumentCaptor<CourseWaypoint> waypointCaptor = ArgumentCaptor.forClass(CourseWaypoint.class);
        verify(adminCourseMapper, times(waypointCount)).insertCourseWaypoint(waypointCaptor.capture());
        if (waypointCount == 1) {
            assertThat(waypointCaptor.getValue().getPlaceNo()).isEqualTo(5L);
            assertThat(waypointCaptor.getValue().getWaypointSequence()).isEqualTo(1);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void routeLookupFailurePreventsCourseAndWaypointWrites(boolean updating) {
        when(adminCourseMapper.countExistingPlaces(anyList())).thenReturn(4);
        when(courseService.getRecommendedRoute(any(RouteSearchRequest.class)))
                .thenThrow(new NotFoundException("순례길 코스 경로를 찾을 수 없습니다."));
        if (updating) {
            when(adminCourseMapper.countCourseByNo(101L)).thenReturn(1);
        }

        assertThatThrownBy(() -> {
            if (updating) {
                adminCourseService.updateCourse(101L, request(List.of(5L, 8L)));
            } else {
                adminCourseService.saveCourse(request(List.of(5L, 8L)));
            }
        }).isInstanceOf(NotFoundException.class);

        verify(adminCourseMapper, never()).insertCourse(any(Course.class));
        verify(adminCourseMapper, never()).updateCourse(any(Course.class));
        verify(adminCourseMapper, never()).deleteCourseWaypoints(any());
        verify(adminCourseMapper, never()).insertCourseWaypoint(any(CourseWaypoint.class));
    }

    private static Stream<List<Long>> waypointsWithoutOrdering() {
        return Stream.of(null, List.of(), List.of(5L));
    }

    private RouteResponse routeResponse(List<Long> waypointPlaceNos) {
        return RouteResponse.builder()
                .waypoints(waypointPlaceNos.stream()
                        .map(placeNo -> PlaceResponse.builder().placeNo(placeNo).build())
                        .toList())
                .build();
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
                "전통 사찰과 웰니스 관광지를 둘러보는 코스입니다.",
                1L,
                waypointPlaceNos,
                20L);
    }

    @Test
    void getCourseReturnsInactiveCourseAndOrderedWaypointsForEditing() {
        AdminCourseDetailResponse detail = new AdminCourseDetailResponse();
        detail.setCourseNo(101L);
        detail.setActive("N");
        detail.setStartPlaceNo(1L);
        detail.setEndPlaceNo(20L);
        when(adminCourseMapper.selectCourseDetail(101L)).thenReturn(detail);
        when(adminCourseMapper.selectWaypointPlaceNos(101L)).thenReturn(List.of(8L, 5L));

        AdminCourseDetailResponse result = adminCourseService.getCourse(101L);

        assertThat(result.getActive()).isEqualTo("N");
        assertThat(result.getStartPlaceNo()).isEqualTo(1L);
        assertThat(result.getEndPlaceNo()).isEqualTo(20L);
        assertThat(result.getWaypointPlaceNos()).containsExactly(8L, 5L);
    }

    @Test
    void getCourseRejectsMissingCourseWithoutReadingWaypoints() {
        when(adminCourseMapper.selectCourseDetail(999L)).thenReturn(null);
        assertThatThrownBy(() -> adminCourseService.getCourse(999L))
                .isInstanceOf(NotFoundException.class);
        verify(adminCourseMapper, never()).selectWaypointPlaceNos(any());
    }

    @Test
    void getCourseRejectsInvalidNumberBeforeQuerying() {
        assertThatThrownBy(() -> adminCourseService.getCourse(0L))
                .isInstanceOf(BadRequestException.class);
        verify(adminCourseMapper, never()).selectCourseDetail(any());
    }
}
