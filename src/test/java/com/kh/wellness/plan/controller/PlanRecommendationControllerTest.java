package com.kh.wellness.plan.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kh.wellness.auth.model.vo.CustomUserDetails;
import com.kh.wellness.exception.UnauthorizedException;
import com.kh.wellness.plan.model.dto.NearbyPlaceRecommendationResponse;
import com.kh.wellness.plan.model.service.PlanRecommendationService;

import static org.mockito.Mockito.mock;

class PlanRecommendationControllerTest {

    private PlanRecommendationService recommendationService;
    private PlanRecommendationController controller;

    @BeforeEach
    void setUp() {
        recommendationService = mock(PlanRecommendationService.class);
        controller = new PlanRecommendationController(recommendationService);
    }

    @Test
    void 로그인_회원의_좌표와_필터를_Service에_전달한다() {
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .memberNo(10L)
                .username("member@example.com")
                .build();
        var expected = List.of(NearbyPlaceRecommendationResponse.builder()
                .placeNo(1L)
                .placeName("추천 장소")
                .build());
        when(recommendationService.findNearbyPlaces(
                10L,
                126.7155,
                37.6153,
                6L,
                List.of(2L, 3L)))
                .thenReturn(expected);

        var response = controller.findNearbyPlaces(
                userDetails,
                126.7155,
                37.6153,
                6L,
                List.of(2L, 3L));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getData()).isEqualTo(expected);
        verify(recommendationService).findNearbyPlaces(
                10L,
                126.7155,
                37.6153,
                6L,
                List.of(2L, 3L));
    }

    @Test
    void 인증_정보가_없으면_추천_조회에_진입하지_않는다() {
        assertThatThrownBy(() -> controller.findNearbyPlaces(
                null,
                126.7155,
                37.6153,
                null,
                null))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("로그인이 필요한 기능입니다.");

        verifyNoInteractions(recommendationService);
    }
}
