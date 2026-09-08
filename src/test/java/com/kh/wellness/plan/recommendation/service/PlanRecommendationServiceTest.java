package com.kh.wellness.plan.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.place.model.dto.MapPlaceResponse;
import com.kh.wellness.place.model.dto.PlaceTypeOptionResponse;
import com.kh.wellness.place.model.service.PlaceService;
import com.kh.wellness.plan.model.dto.PlanDetailResponse;
import com.kh.wellness.plan.model.service.PlanService;

@ExtendWith(MockitoExtension.class)
class PlanRecommendationServiceTest {

    @Mock
    private PlanService planService;

    @Mock
    private PlaceService placeService;

    private PlanRecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        recommendationService = new PlanRecommendationService(planService, placeService);
    }

    @Test
    void 주변_장소를_타입과_제외_목록으로_필터링하고_거리순으로_반환한다() {
        double originX = 126.7155;
        double originY = 37.6153;
        when(planService.findNearbyPlaces(10L, originX, originY)).thenReturn(List.of(
                nearby(1L, "가까운 한식당", 17L, 126.7160, 37.6153),
                nearby(2L, "이미 선택한 한식당", 17L, 126.7165, 37.6153),
                nearby(3L, "관광지", 18L, 126.7160, 37.6153),
                nearby(4L, "삭제 장소", 17L, 126.7156, 37.6153),
                nearby(5L, "먼 한식당", 17L, 126.7250, 37.6153)));
        when(placeService.findMapPlaces()).thenReturn(List.of(
                active(1L, "가까운 한식당", "음식점", "한식", 126.7160, 37.6153, "near.jpg"),
                active(2L, "이미 선택한 한식당", "음식점", "한식", 126.7165, 37.6153, "selected.jpg"),
                active(3L, "관광지", "주요관광지", "김포 TOP 10", 126.7160, 37.6153, "tour.jpg"),
                active(5L, "먼 한식당", "음식점", "한식", 126.7250, 37.6153, "far.jpg")));
        when(placeService.findPlaceTypeOptions()).thenReturn(List.of(
                type(6L, "음식점", 17L, "한식"),
                type(1L, "주요관광지", 18L, "김포 TOP 10")));

        var result = recommendationService.findNearbyPlaces(
                10L,
                originX,
                originY,
                6L,
                List.of(2L));

        assertThat(result)
                .extracting(
                        response -> response.getPlaceNo(),
                        response -> response.getTypeNo(),
                        response -> response.getType(),
                        response -> response.getTypeDetail(),
                        response -> response.getImageUrl())
                .containsExactly(
                        tuple(1L, 6L, "음식점", "한식", "near.jpg"),
                        tuple(5L, 6L, "음식점", "한식", "far.jpg"));
        assertThat(result.get(0).getDistanceMeters()).isLessThan(result.get(1).getDistanceMeters());
        verify(planService).findNearbyPlaces(10L, originX, originY);
    }

    @Test
    void 타입을_지정하지_않으면_모든_활성_주변_장소를_반환한다() {
        when(planService.findNearbyPlaces(10L, 126.7155, 37.6153)).thenReturn(List.of(
                nearby(1L, "음식점", 17L, 126.7160, 37.6153),
                nearby(2L, "관광지", 18L, 126.7170, 37.6153)));
        when(placeService.findMapPlaces()).thenReturn(List.of(
                active(1L, "음식점", "음식점", "한식", 126.7160, 37.6153, null),
                active(2L, "관광지", "주요관광지", "김포 TOP 10", 126.7170, 37.6153, null)));
        when(placeService.findPlaceTypeOptions()).thenReturn(List.of(
                type(6L, "음식점", 17L, "한식"),
                type(1L, "주요관광지", 18L, "김포 TOP 10")));

        var result = recommendationService.findNearbyPlaces(
                10L,
                126.7155,
                37.6153,
                null,
                null);

        assertThat(result).extracting(response -> response.getPlaceNo()).containsExactly(1L, 2L);
    }

    @Test
    void 잘못된_좌표는_기존_Service를_호출하기_전에_거부한다() {
        assertThatThrownBy(() -> recommendationService.findNearbyPlaces(
                10L,
                181.0,
                37.6153,
                null,
                null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("X 좌표는 -180 이상 180 이하의 값이어야 합니다.");

        verifyNoInteractions(planService, placeService);
    }

    @Test
    void 계획_장소가_10개를_초과하면_추천_요청을_거부한다() {
        assertThatThrownBy(() -> recommendationService.findNearbyPlaces(
                10L,
                126.7155,
                37.6153,
                null,
                List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("계획 장소는 최대 10개까지 지정할 수 있습니다.");

        verifyNoInteractions(planService, placeService);
    }

    private PlanDetailResponse nearby(
            Long placeNo,
            String placeName,
            Long typeDetailNo,
            Double xAxis,
            Double yAxis) {
        PlanDetailResponse response = new PlanDetailResponse();
        response.setPlaceNo(placeNo);
        response.setPlaceName(placeName);
        response.setPlaceDescription(placeName + " 설명");
        response.setAddr("김포시");
        response.setXAxis(xAxis);
        response.setYAxis(yAxis);
        response.setTypeDetailNo(typeDetailNo);
        return response;
    }

    private MapPlaceResponse active(
            Long placeNo,
            String placeName,
            String typeName,
            String typeDetail,
            Double xAxis,
            Double yAxis,
            String imageUrl) {
        return MapPlaceResponse.builder()
                .placeNo(placeNo)
                .placeName(placeName)
                .placeDescription(placeName + " 설명")
                .addr("김포시")
                .type(typeName)
                .typeDetail(typeDetail)
                .xAxis(xAxis)
                .yAxis(yAxis)
                .imageUrl(imageUrl)
                .build();
    }

    private PlaceTypeOptionResponse type(
            Long typeNo,
            String typeName,
            Long typeDetailNo,
            String typeDetailName) {
        return PlaceTypeOptionResponse.builder()
                .typeNo(typeNo)
                .type(typeName)
                .typeDetailNo(typeDetailNo)
                .typeDetailContent(typeDetailName)
                .build();
    }
}
