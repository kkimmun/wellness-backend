package com.kh.wellness.place.model.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import com.kh.wellness.place.model.dao.PlaceMapper;
import com.kh.wellness.place.model.dto.MapPlaceResponse;
import com.kh.wellness.place.model.dto.PlaceDetailResponse;
import com.kh.wellness.place.model.dto.PlaceImageResponse;
import com.kh.wellness.place.model.vo.MapPlace;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

    @Mock
    private PlaceMapper placeMapper;

    private PlaceService placeService;

    @BeforeEach
    void setUp() {
        placeService = new PlaceService(placeMapper);
    }

    @Test
    void 지도_핀은_DB의_삭제되지_않은_장소_조회결과를_반환한다() {
        when(placeMapper.findMapPlaces()).thenReturn(List.of(mapPlace()));

        List<MapPlaceResponse> response = placeService.findMapPlaces();

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getPlaceNo()).isEqualTo(7L);
        assertThat(response.getFirst().getXAxis()).isEqualTo(126.7109331831);
        assertThat(response.getFirst().getYAxis()).isEqualTo(37.61085802);
        assertThat(response.getFirst().getImageUrl()).isEqualTo("https://bucket/places/jangneung.jpg");
        verify(placeMapper).findMapPlaces();
    }

    @Test
    void 장소_상세는_등록순서의_이미지_전체를_반환한다() {
        PlaceDetailResponse detail = new PlaceDetailResponse();
        detail.setPlaceNo(1062L);
        detail.setPlaceName("애기봉");
        detail.setPlaceDescription("애기봉 설명");
        List<PlaceImageResponse> images = List.of(
                new PlaceImageResponse(1, "https://bucket/places/first.png"),
                new PlaceImageResponse(2, "https://bucket/places/second.png"));
        when(placeMapper.selectPlaceDetail(1062L)).thenReturn(detail);
        when(placeMapper.selectPlaceImages(1062L)).thenReturn(images);

        PlaceDetailResponse response = placeService.getPlaceDetail(1062L);

        assertThat(response.getPlaceDescription()).isEqualTo("애기봉 설명");
        assertThat(response.getPlaceImages())
                .extracting(PlaceImageResponse::getImageUrl)
                .containsExactly(
                        "https://bucket/places/first.png",
                        "https://bucket/places/second.png");
        verify(placeMapper).selectPlaceImages(1062L);
    }

    @Test
    void 삭제됐거나_없는_장소의_상세는_404_예외를_발생시킨다() {
        when(placeMapper.selectPlaceDetail(9999L)).thenReturn(null);

        assertThatThrownBy(() -> placeService.getPlaceDetail(9999L))
                .isInstanceOf(com.kh.wellness.exception.NotFoundException.class)
                .hasMessage("존재하지 않는 관광지입니다.");
    }

    @Test
    void 타입_값의_앞뒤_공백을_제거하고_장소를_조회한다() {
        when(placeMapper.findMapPlacesByType("주요관광지")).thenReturn(List.of(mapPlace()));

        List<MapPlaceResponse> response = placeService.findMapPlacesByType("  주요관광지  ");

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getType()).isEqualTo("주요관광지");
        assertThat(response.getFirst().getTypeDetail()).isEqualTo("역사유적");
        verify(placeMapper).findMapPlacesByType("주요관광지");
    }

    @Test
    void 태그_값의_앞뒤_공백을_제거하고_장소를_조회한다() {
        when(placeMapper.findMapPlacesByTag("반려동물")).thenReturn(List.of(mapPlace()));

        List<MapPlaceResponse> response = placeService.findMapPlacesByTag(" 반려동물 ");

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getPlaceNo()).isEqualTo(7L);
        verify(placeMapper).findMapPlacesByTag("반려동물");
    }

    @Test
    void 타입이나_태그가_공백이면_400_예외를_발생시킨다() {
        assertThatThrownBy(() -> placeService.findMapPlacesByType("   "))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("타입은 필수입니다.");
        assertThatThrownBy(() -> placeService.findMapPlacesByTag(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("태그는 필수입니다.");

        verifyNoInteractions(placeMapper);
    }

    private MapPlace mapPlace() {
        return MapPlace.builder()
                .placeNo(7L)
                .placeName("김포장릉")
                .placeDescription("김포의 조선 왕릉")
                .addr("경기도 김포시 장릉로 79")
                .addrDetail("풍무동 666-3")
                .phone("031-984-2897")
                .type("주요관광지")
                .typeDetail("역사유적")
                .viewCount(12L)
                .xAxis(126.7109331831)
                .yAxis(37.61085802)
                .imageUrl("https://bucket/places/jangneung.jpg")
                .build();
    }
}
