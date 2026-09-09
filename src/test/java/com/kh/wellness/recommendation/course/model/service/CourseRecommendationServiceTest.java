package com.kh.wellness.recommendation.course.model.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.recommendation.course.model.dto.CourseRecommendationCandidateRow;
import com.kh.wellness.recommendation.course.model.dto.CourseRecommendationRequest;
import com.kh.wellness.recommendation.course.model.dao.CourseRecommendationMapper;

@ExtendWith(MockitoExtension.class)
class CourseRecommendationServiceTest {

    @Mock
    private CourseRecommendationMapper mapper;

    private CourseRecommendationService service;

    @BeforeEach
    void setUp() {
        service = new CourseRecommendationService(mapper);
        when(mapper.findAllCandidates()).thenReturn(List.of(
                row(1L, "관광지 A", "관광지", 126.7160, 37.6153, 11L),
                row(2L, "관광지 B", "주요관광지", 126.7170, 37.6153, 10L),
                row(3L, "식당 A", "음식점", 126.7180, 37.6153, 4L),
                row(4L, "카페 B", "음식점", 126.7190, 37.6153, 4L),
                row(5L, "체험 A", "체험", 126.7200, 37.6153, 3L),
                row(6L, "체험 B", "체육시설", 126.7210, 37.6153, 14L)));
    }

    @Test
    void 선택장소와_중간_음식점을_포함한_정확한_개수의_코스를_만든다() {
        CourseRecommendationRequest request = request();
        request.setPlaceCount(3);
        request.setPreferredPlaceNos(List.of(1L));
        request.setTagNos(List.of(4L));

        var result = service.recommend(7L, request);

        assertThat(result.getPlaces()).hasSize(3);
        assertThat(result.getPlaces()).extracting("placeNo").contains(1L);
        assertThat(result.getPlaces().get(1).getType()).isEqualTo("음식점");
        assertThat(result.getCourseSignature()).isNotBlank();
    }

    @Test
    void 다시추천은_이미_본_코스와_다른_코스를_반환한다() {
        CourseRecommendationRequest firstRequest = request();
        firstRequest.setPlaceCount(3);
        var first = service.recommend(7L, firstRequest);

        CourseRecommendationRequest nextRequest = request();
        nextRequest.setPlaceCount(3);
        nextRequest.setExcludeCourseSignatures(List.of(first.getCourseSignature()));
        var next = service.recommend(7L, nextRequest);

        assertThat(next.getCourseSignature()).isNotEqualTo(first.getCourseSignature());
    }

    @Test
    void 가능한_다른_코스가_없으면_명확한_오류를_반환한다() {
        when(mapper.findAllCandidates()).thenReturn(List.of(
                row(1L, "관광지 A", "관광지", 126.7160, 37.6153, 11L),
                row(3L, "식당 A", "음식점", 126.7180, 37.6153, 4L),
                row(5L, "체험 A", "체험", 126.7200, 37.6153, 3L)));
        CourseRecommendationRequest request = request();
        request.setPlaceCount(3);
        var first = service.recommend(7L, request);
        request.setExcludeCourseSignatures(List.of("1-3-5", "5-3-1"));

        assertThatThrownBy(() -> service.recommend(7L, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 주요관광지는_출발지에서_3km를_넘어도_10km_이내면_추천할_수_있다() {
        when(mapper.findAllCandidates()).thenReturn(List.of(
                rowWithTypeNo(1L, "먼 주요관광지", "주요관광지", 1L, 126.7650, 37.6153, 11L),
                rowWithTypeNo(2L, "인근 음식점", "음식점", 6L, 126.7660, 37.6153, 4L),
                rowWithTypeNo(3L, "인근 관광지", "관광지", 3L, 126.7670, 37.6153, 10L)));
        CourseRecommendationRequest request = request();
        request.setPlaceCount(3);
        request.setPreferredPlaceNos(List.of(1L));

        var result = service.recommend(7L, request);

        assertThat(result.getPlaces()).extracting("placeNo").contains(1L);
    }

    @Test
    void 음식점은_출발지와_이전장소에서_3km를_넘으면_추천하지_않는다() {
        when(mapper.findAllCandidates()).thenReturn(List.of(
                rowWithTypeNo(1L, "가까운 관광지", "관광지", 3L, 126.7160, 37.6153, 11L),
                rowWithTypeNo(2L, "가까운 주요관광지", "주요관광지", 1L, 126.7170, 37.6153, 10L),
                rowWithTypeNo(3L, "먼 음식점", "음식점", 6L, 126.7650, 37.6153, 4L)));
        CourseRecommendationRequest request = request();
        request.setPlaceCount(3);

        assertThatThrownBy(() -> service.recommend(7L, request))
                .isInstanceOf(NotFoundException.class);
    }

    private CourseRecommendationRequest request() {
        CourseRecommendationRequest request = new CourseRecommendationRequest();
        request.setStartX(126.7155);
        request.setStartY(37.6153);
        return request;
    }

    private CourseRecommendationCandidateRow row(
            Long placeNo,
            String name,
            String type,
            double xAxis,
            double yAxis,
            Long tagNo) {
        CourseRecommendationCandidateRow row = new CourseRecommendationCandidateRow();
        row.setPlaceNo(placeNo);
        row.setPlaceName(name);
        row.setPlaceDescription(name + " 설명");
        row.setAddr("김포시");
        row.setXAxis(xAxis);
        row.setYAxis(yAxis);
        row.setTypeNo(placeNo);
        row.setType(type);
        row.setTypeDetailNo(placeNo);
        row.setTypeDetail(type);
        row.setViewCount(10L);
        row.setTagNo(tagNo);
        row.setTagContent("태그 " + tagNo);
        return row;
    }

    private CourseRecommendationCandidateRow rowWithTypeNo(
            Long placeNo,
            String name,
            String type,
            Long typeNo,
            double xAxis,
            double yAxis,
            Long tagNo) {
        CourseRecommendationCandidateRow row = row(
                placeNo, name, type, xAxis, yAxis, tagNo);
        row.setTypeNo(typeNo);
        return row;
    }
}
