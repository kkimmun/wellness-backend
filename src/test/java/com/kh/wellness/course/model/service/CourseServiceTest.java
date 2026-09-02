package com.kh.wellness.course.model.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kh.wellness.ai.model.dto.CourseContent;
import com.kh.wellness.ai.model.service.OllamaClient;
import com.kh.wellness.common.page.PageResponse;
import com.kh.wellness.course.model.dao.CourseMapper;
import com.kh.wellness.course.model.dto.CourseListResponse;
import com.kh.wellness.course.model.dto.CourseListRow;
import com.kh.wellness.course.model.dto.CourseResponse;
import com.kh.wellness.course.model.dto.CustomCourseRequest;
import com.kh.wellness.course.model.dto.PlaceDto;
import com.kh.wellness.course.model.dto.WaypointDto;
import com.kh.wellness.course.model.enums.CourseTag;
import com.kh.wellness.place.model.service.PlaceService;
import com.kh.wellness.route.model.service.RouteService;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseMapper courseMapper;
    @Mock
    private RouteService routeService;
    @Mock
    private PlaceService placeService;
    @Mock
    private OllamaClient ollamaClient;

    private CourseService courseService;

    @BeforeEach
    void setUp() {
        courseService = new CourseService(routeService, courseMapper, placeService, ollamaClient);
    }

    @Test
    void getCoursesMapsStartAndEndPlaces() {
        CourseListRow row = new CourseListRow(
                1L,
                "문수산 마음쉼길",
                10L,
                "김포시청",
                101L,
                "문수산성 사찰",
                210,
                "자연과 휴식을 중심으로 구성한 힐링 순례 코스입니다.");
        when(courseMapper.countActiveCourses()).thenReturn(1L);
        when(courseMapper.selectActiveCourses(0L, 5)).thenReturn(List.of(row));

        PageResponse<CourseListResponse> result = courseService.getCourses(1);

        assertThat(result.getContent()).hasSize(1);
        CourseListResponse course = result.getContent().getFirst();
        assertThat(course.getStartPlace().getPlaceNo()).isEqualTo(10L);
        assertThat(course.getStartPlace().getPlaceName()).isEqualTo("김포시청");
        assertThat(course.getEndPlace().getPlaceNo()).isEqualTo(101L);
        assertThat(course.getEndPlace().getPlaceName()).isEqualTo("문수산성 사찰");
        verify(courseMapper).selectActiveCourses(0L, 5);
    }

    @Test
    void getCourseIncludesMiddleWaypoints() {
        CourseResponse course = CourseResponse.builder()
                .courseNo(1L)
                .startPlace(10L)
                .endPlace(30L)
                .courseName("문수산 마음쉴길")
                .estimatedTime(210)
                .description("자연과 휴식을 중심으로 구성한 힐링 순례 코스입니다.")
                .build();
        WaypointDto waypoint = new WaypointDto(
                100L, 1L, 20L, 1, "중간 관광지", 126.1, 37.1, null);
        PlaceDto endPlace = PlaceDto.builder().placeNo(30L).imageUrl("end.jpg").build();
        when(courseMapper.selectByCourseNo(1L)).thenReturn(course);
        when(courseMapper.selectWaypointBycourseNo(1L)).thenReturn(List.of(waypoint));
        when(placeService.selectByPlaceNo(30L)).thenReturn(endPlace);

        CourseResponse result = courseService.getCourse(1L);

        assertThat(result.getWaypoints()).containsExactly(waypoint);
        assertThat(result.getEndPlaceImg()).isEqualTo("end.jpg");
        verify(courseMapper).selectWaypointBycourseNo(1L);
    }

    @Test
    void getCustomCourseUsesOllamaContent() {
        CustomCourseRequest request = new CustomCourseRequest(
                30L, 126.1, 37.1, List.of(CourseTag.힐링), List.of(20L));
        PlaceDto waypoint = PlaceDto.builder().placeNo(20L).placeName("애기봉").build();
        PlaceDto endPlace = PlaceDto.builder()
                .placeNo(30L).placeName("문수산성").imageUrl("end.jpg").build();
        CourseContent content = new CourseContent(
                "김포 마음쉼길", "애기봉에서 여유를 느껴보세요.\n문수산성에서 자연과 역사를 만납니다.");

        when(placeService.selectByPlaceNo(20L)).thenReturn(waypoint);
        when(placeService.selectByPlaceNo(30L)).thenReturn(endPlace);
        when(ollamaClient.generateCourseContent(
                List.of("애기봉", "문수산성"), List.of(CourseTag.힐링))).thenReturn(content);

        CourseResponse result = courseService.getCustomCourse(request);

        assertThat(result.getCourseName()).isEqualTo("김포 마음쉼길");
        assertThat(result.getDescription()).contains("\n");
        assertThat(result.getPlaces()).containsExactly(waypoint);
        assertThat(result.getEndPlace()).isEqualTo(30L);
    }
}
