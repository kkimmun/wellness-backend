package com.kh.wellness.recommendation.course.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kh.wellness.auth.model.vo.CustomUserDetails;
import com.kh.wellness.exception.UnauthorizedException;
import com.kh.wellness.recommendation.course.model.dto.CourseRecommendationRequest;
import com.kh.wellness.recommendation.course.model.dto.CourseRecommendationResponse;
import com.kh.wellness.recommendation.course.model.service.CourseRecommendationService;

class CourseRecommendationControllerTest {

    private CourseRecommendationService service;
    private CourseRecommendationController controller;

    @BeforeEach
    void setUp() {
        service = mock(CourseRecommendationService.class);
        controller = new CourseRecommendationController(service);
    }

    @Test
    void 로그인_회원의_추천_조건을_Service에_전달한다() {
        CustomUserDetails user = CustomUserDetails.builder()
                .memberNo(7L)
                .username("member@example.com")
                .build();
        CourseRecommendationRequest request = new CourseRecommendationRequest();
        request.setStartX(126.7155);
        request.setStartY(37.6153);
        CourseRecommendationResponse expected = CourseRecommendationResponse.builder()
                .courseSignature("1-2-3")
                .placeCount(3)
                .places(List.of())
                .build();
        when(service.recommend(7L, request)).thenReturn(expected);

        var response = controller.recommendCourse(user, request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(expected);
        verify(service).recommend(7L, request);
    }

    @Test
    void 비회원은_추천_Service를_호출하지_않는다() {
        CourseRecommendationRequest request = new CourseRecommendationRequest();

        assertThatThrownBy(() -> controller.recommendCourse(null, request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("로그인이 필요한 기능입니다.");
        verifyNoInteractions(service);
    }
}
