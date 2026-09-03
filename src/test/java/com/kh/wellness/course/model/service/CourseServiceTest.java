package com.kh.wellness.course.model.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import com.kh.wellness.course.model.dto.WaypointsRequest;
import com.kh.wellness.course.model.enums.CourseTag;
import com.kh.wellness.exception.InternalServerException;
import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.place.model.service.PlaceService;
import com.kh.wellness.route.model.dto.CoordinateResponse;
import com.kh.wellness.route.model.dto.PlaceCandidate;
import com.kh.wellness.route.model.dto.RouteResponse;
import com.kh.wellness.route.model.dto.RouteResultResponse;
import com.kh.wellness.route.model.dto.RouteSearchRequest;
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
        assertThat(result.getEndPlaceImg()).isEqualTo("end.jpg");
    }

    @Test
    void getCustomCourseUsesOnlyEndPlaceWhenWaypointsAreNull() {
        CustomCourseRequest request = new CustomCourseRequest(
                30L, 126.1, 37.1, List.of(CourseTag.힐링), null);
        PlaceDto endPlace = PlaceDto.builder()
                .placeNo(30L)
                .placeName("문수산성")
                .imageUrl("end.jpg")
                .build();
        CourseContent content = new CourseContent(
                "문수산 힐링길", "문수산성에서 자연과 역사를 만납니다.");
        when(placeService.selectByPlaceNo(30L)).thenReturn(endPlace);
        when(ollamaClient.generateCourseContent(
                List.of("문수산성"), List.of(CourseTag.힐링))).thenReturn(content);

        CourseResponse result = courseService.getCustomCourse(request);

        assertThat(result.getPlaces()).isEmpty();
        assertThat(result.getCourseName()).isEqualTo("문수산 힐링길");
        assertThat(result.getDescription()).isEqualTo("문수산성에서 자연과 역사를 만납니다.");
        assertThat(result.getEndPlace()).isEqualTo(30L);
        assertThat(result.getEndPlaceImg()).isEqualTo("end.jpg");
        verify(placeService).selectByPlaceNo(30L);
    }

    @Test
    void getRecommendedRouteComparesEveryWaypointOrder() {
        RouteSearchRequest request = routeRequest(List.of(10L, 15L));
        RouteResponse longerRoute = routeResponse(12_034);
        RouteResponse shorterRoute = routeResponse(9_500);
        when(routeService.findRoutes(any(RouteSearchRequest.class)))
                .thenAnswer(invocation -> {
                    RouteSearchRequest candidate = invocation.getArgument(0);
                    return candidate.getWaypointPlaceNos().equals(List.of(10L, 15L))
                            ? longerRoute
                            : shorterRoute;
                });

        RouteResponse result = courseService.getRecommendedRoute(request);

        assertThat(result).isSameAs(shorterRoute);
        assertThat(request.getWaypointPlaceNos()).containsExactly(10L, 15L);
        ArgumentCaptor<RouteSearchRequest> captor =
                ArgumentCaptor.forClass(RouteSearchRequest.class);
        verify(routeService, times(2)).findRoutes(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(RouteSearchRequest::getWaypointPlaceNos)
                .containsExactlyInAnyOrder(List.of(10L, 15L), List.of(15L, 10L));
    }

    @Test
    void getRecommendedRouteSearchesOnceWithoutWaypointsAndCopiesRequestValues() {
        RouteSearchRequest request = routeRequest(null);
        RouteResponse route = routeResponse(8_500);
        when(routeService.findRoutes(any(RouteSearchRequest.class))).thenReturn(route);

        RouteResponse result = courseService.getRecommendedRoute(request);

        assertThat(result).isSameAs(route);
        ArgumentCaptor<RouteSearchRequest> captor =
                ArgumentCaptor.forClass(RouteSearchRequest.class);
        verify(routeService).findRoutes(captor.capture());
        RouteSearchRequest copiedRequest = captor.getValue();
        assertThat(copiedRequest).isNotSameAs(request);
        assertThat(copiedRequest.getStartPlaceNo()).isEqualTo(5L);
        assertThat(copiedRequest.getEndPlaceNo()).isEqualTo(25L);
        assertThat(copiedRequest.getStartX()).isEqualTo(126.7156);
        assertThat(copiedRequest.getStartY()).isEqualTo(37.6152);
        assertThat(copiedRequest.getEndX()).isEqualTo(126.6801);
        assertThat(copiedRequest.getEndY()).isEqualTo(37.7214);
        assertThat(copiedRequest.getTransportType()).isEqualTo("WALK");
        assertThat(copiedRequest.getRouteOption()).isEqualTo("SHORTEST");
        assertThat(copiedRequest.getTransitType()).isEqualTo("SUBWAY");
        assertThat(copiedRequest.getSortType()).isEqualTo("RECOMMEND");
        assertThat(copiedRequest.getWaypointPlaceNos()).isEmpty();
        verify(placeService).selectByPlaceNo(5L);
        verify(placeService).selectByPlaceNo(25L);
    }

    @Test
    void getRecommendedRouteUsesShortestDistanceWithinEachRouteResponse() {
        RouteSearchRequest request = routeRequest(List.of(10L, 15L));
        RouteResponse firstOrder = routeResponse(12_000, 7_000);
        RouteResponse secondOrder = routeResponse(8_000);
        when(routeService.findRoutes(any(RouteSearchRequest.class)))
                .thenAnswer(invocation -> {
                    RouteSearchRequest candidate = invocation.getArgument(0);
                    return candidate.getWaypointPlaceNos().equals(List.of(10L, 15L))
                            ? firstOrder
                            : secondOrder;
                });

        RouteResponse result = courseService.getRecommendedRoute(request);

        assertThat(result).isSameAs(firstOrder);
        verify(routeService, times(2)).findRoutes(any(RouteSearchRequest.class));
    }

    @Test
    void getRecommendedRouteThrowsWhenRouteResponseIsInvalid() {
        RouteSearchRequest request = routeRequest(List.of());
        when(routeService.findRoutes(any(RouteSearchRequest.class))).thenReturn(null);

        assertThatThrownBy(() -> courseService.getRecommendedRoute(request))
                .isInstanceOf(InternalServerException.class)
                .hasMessage("카카오 경로 응답이 올바르지 않습니다.");
    }

    @Test
    void getRecommendedRouteContinuesWhenOneOrderHasNoRoute() {
        RouteSearchRequest request = routeRequest(List.of(10L, 15L));
        RouteResponse availableRoute = routeResponse(10_000);
        when(routeService.findRoutes(any(RouteSearchRequest.class)))
                .thenAnswer(invocation -> {
                    RouteSearchRequest candidate = invocation.getArgument(0);
                    if (candidate.getWaypointPlaceNos().equals(List.of(10L, 15L))) {
                        throw new NotFoundException("경로를 찾을 수 없습니다.");
                    }
                    return availableRoute;
                });

        RouteResponse result = courseService.getRecommendedRoute(request);

        assertThat(result).isSameAs(availableRoute);
        verify(routeService, times(2)).findRoutes(any(RouteSearchRequest.class));
    }

    @Test
    void getRecommendedRouteThrowsWhenEveryOrderHasNoRoute() {
        RouteSearchRequest request = routeRequest(List.of(10L, 15L));
        when(routeService.findRoutes(any(RouteSearchRequest.class)))
                .thenThrow(new NotFoundException("경로를 찾을 수 없습니다."));

        assertThatThrownBy(() -> courseService.getRecommendedRoute(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("순례길 코스 경로를 찾을 수 없습니다.");

        verify(routeService, times(2)).findRoutes(any(RouteSearchRequest.class));
    }

    @Test
    void getRecommendedRouteRejectsMissingPlaceBeforeRouteLookup() {
        RouteSearchRequest request = routeRequest(List.of(10L, 15L));
        when(placeService.selectByPlaceNo(any(Long.class)))
                .thenAnswer(invocation -> {
                    Long placeNo = invocation.getArgument(0);
                    if (placeNo.equals(15L)) {
                        throw new NotFoundException("존재하지 않는 관광지입니다.");
                    }
                    return null;
                });

        assertThatThrownBy(() -> courseService.getRecommendedRoute(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("선택한 관광지 정보를 찾을 수 없습니다.");

        verifyNoInteractions(routeService);
    }

    @Test
    void getWaypointsCalculatesScoresAndSortsCandidates() {
        WaypointsRequest request = waypointsRequest(List.of(CourseTag.힐링, CourseTag.자연));
        PlaceDto partialMatch = place(10L, "애기봉", 126.7000, 37.6000,
                List.of(CourseTag.힐링));
        PlaceDto fullMatch = place(20L, "문수산성", 126.7000, 37.6000,
                List.of(CourseTag.힐링, CourseTag.자연));
        when(courseMapper.selectByTags(request.getTags()))
                .thenReturn(List.of(partialMatch, fullMatch));
        when(routeService.findRoutes(any(RouteSearchRequest.class)))
                .thenReturn(routeResponseWithPath(coordinate(126.7000, 37.6000)));

        List<PlaceCandidate> result = courseService.getWaypoints(request);

        assertThat(result)
                .extracting(PlaceCandidate::getPlaceName)
                .containsExactly("문수산성", "애기봉");
        assertThat(result.get(0).getPlace()).isSameAs(fullMatch);
        assertThat(result.get(0).getImageUrl()).isEqualTo("20.jpg");
        assertThat(result.get(0).getTags()).containsExactly(CourseTag.힐링, CourseTag.자연);
        assertThat(result.get(0).getDistance()).isZero();
        assertThat(result.get(0).getDistanceScore()).isEqualTo(1.0);
        assertThat(result.get(0).getTagScore()).isEqualTo(1.0);
        assertThat(result.get(0).getTotalScore()).isEqualTo(1.0);
        assertThat(result.get(1).getTagScore()).isEqualTo(0.5);
        assertThat(result.get(1).getTotalScore()).isEqualTo(0.65);

        ArgumentCaptor<RouteSearchRequest> captor =
                ArgumentCaptor.forClass(RouteSearchRequest.class);
        verify(routeService).findRoutes(captor.capture());
        RouteSearchRequest routeRequest = captor.getValue();
        assertThat(routeRequest.getStartX()).isEqualTo(request.getStartX());
        assertThat(routeRequest.getStartY()).isEqualTo(request.getStartY());
        assertThat(routeRequest.getEndPlaceNo()).isEqualTo(request.getEndPlaceNo());
        assertThat(routeRequest.getTransportType()).isEqualTo("WALK");
        assertThat(routeRequest.getRouteOption()).isEqualTo("BROAD_FIRST");
    }

    @Test
    void getWaypointsAppliesDistanceScoreBandsAndExcludesPlacesOverTwoKilometers() {
        WaypointsRequest request = waypointsRequest(List.of(CourseTag.힐링));
        PlaceDto within500Meters = place(10L, "500m 이내", 0.0, 0.004,
                List.of(CourseTag.힐링));
        PlaceDto within1000Meters = place(20L, "1km 이내", 0.0, 0.006,
                List.of(CourseTag.힐링));
        PlaceDto within1500Meters = place(30L, "1.5km 이내", 0.0, 0.011,
                List.of(CourseTag.힐링));
        PlaceDto within2000Meters = place(40L, "2km 이내", 0.0, 0.016,
                List.of(CourseTag.힐링));
        PlaceDto over2000Meters = place(50L, "2km 초과", 0.0, 0.019,
                List.of(CourseTag.힐링));
        when(courseMapper.selectByTags(request.getTags())).thenReturn(List.of(
                within2000Meters,
                over2000Meters,
                within1500Meters,
                within1000Meters,
                within500Meters));
        when(routeService.findRoutes(any(RouteSearchRequest.class)))
                .thenReturn(routeResponseWithPath(coordinate(0.0, 0.0)));

        List<PlaceCandidate> result = courseService.getWaypoints(request);

        assertThat(result)
                .extracting(PlaceCandidate::getPlaceName)
                .containsExactly("500m 이내", "1km 이내", "1.5km 이내", "2km 이내");
        assertThat(result)
                .extracting(PlaceCandidate::getDistanceScore)
                .containsExactly(1.0, 0.7, 0.5, 0.2);
        assertThat(result)
                .extracting(PlaceCandidate::getPlace)
                .doesNotContain(over2000Meters);
    }

    @Test
    void getWaypointsThrowsWhenRouteResultIsEmpty() {
        WaypointsRequest request = waypointsRequest(List.of(CourseTag.힐링));
        when(courseMapper.selectByTags(request.getTags())).thenReturn(List.of());
        when(routeService.findRoutes(any(RouteSearchRequest.class)))
                .thenReturn(RouteResponse.builder().routes(List.of()).build());

        assertThatThrownBy(() -> courseService.getWaypoints(request))
                .isInstanceOf(InternalServerException.class)
                .hasMessage("잠시 후에 다시 시도해주세요.");
    }

    private RouteSearchRequest routeRequest(List<Long> waypointPlaceNos) {
        RouteSearchRequest request = new RouteSearchRequest();
        request.setStartPlaceNo(5L);
        request.setStartX(126.7156);
        request.setStartY(37.6152);
        request.setEndPlaceNo(25L);
        request.setEndX(126.6801);
        request.setEndY(37.7214);
        request.setTransportType("WALK");
        request.setRouteOption("SHORTEST");
        request.setTransitType("SUBWAY");
        request.setSortType("RECOMMEND");
        request.setWaypointPlaceNos(waypointPlaceNos);
        return request;
    }

    private RouteResponse routeResponse(int... totalDistances) {
        return RouteResponse.builder()
                .routes(Arrays.stream(totalDistances)
                        .mapToObj(totalDistance -> RouteResultResponse.builder()
                                .totalDistance(totalDistance)
                                .totalTime(13_200)
                                .build())
                        .toList())
                .build();
    }

    private WaypointsRequest waypointsRequest(List<CourseTag> tags) {
        return new WaypointsRequest(
                30L,
                126.7156,
                37.6152,
                tags,
                180,
                List.of());
    }

    private PlaceDto place(
            Long placeNo,
            String placeName,
            double xAxis,
            double yAxis,
            List<CourseTag> tags) {
        return PlaceDto.builder()
                .placeNo(placeNo)
                .placeName(placeName)
                .xAxis(xAxis)
                .yAxis(yAxis)
                .imageUrl(placeNo + ".jpg")
                .tags(tags)
                .build();
    }

    private CoordinateResponse coordinate(double xAxis, double yAxis) {
        return CoordinateResponse.builder()
                .xAxis(xAxis)
                .yAxis(yAxis)
                .build();
    }

    private RouteResponse routeResponseWithPath(CoordinateResponse... coordinates) {
        return RouteResponse.builder()
                .routes(List.of(RouteResultResponse.builder()
                        .path(List.of(coordinates))
                        .build()))
                .build();
    }
}
