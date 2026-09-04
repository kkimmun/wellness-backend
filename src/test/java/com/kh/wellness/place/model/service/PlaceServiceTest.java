package com.kh.wellness.place.model.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.place.model.dao.PlaceMapper;
import com.kh.wellness.place.model.dto.MapPlaceResponse;
import com.kh.wellness.place.model.dto.PlaceDetailDto;
import com.kh.wellness.place.model.dto.PlaceDetailResponse;
import com.kh.wellness.place.model.dto.PlaceImageDto;
import com.kh.wellness.place.model.dto.PlaceTagDto;
import com.kh.wellness.place.model.vo.MapPlace;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

    @Mock
    private PlaceMapper placeMapper;

    @InjectMocks
    private PlaceService placeService;

    @Test
    void 지도_핀은_DB_조회결과를_응답으로_변환한다() {
        when(placeMapper.findMapPlaces()).thenReturn(List.of(mapPlace()));

        List<MapPlaceResponse> response = placeService.findMapPlaces();

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getPlaceNo()).isEqualTo(7L);
        assertThat(response.getFirst().getImageUrl()).isEqualTo("https://bucket/places/jangneung.jpg");
        verify(placeMapper).findMapPlaces();
    }

    @Test
    void 장소_상세는_이미지와_태그와_회원_북마크를_함께_반환한다() {
        PlaceDetailDto detail = new PlaceDetailDto();
        detail.setPlaceNo(1062L);
        detail.setPlaceName("애기봉");
        detail.setPlaceDescription("애기봉 설명");
        detail.setBookmarkYn("Y");
        PlaceImageDto image = new PlaceImageDto();
        image.setImageUrl("https://bucket/places/aegibong.png");
        PlaceTagDto tag = new PlaceTagDto();

        when(placeMapper.selectPlaceDetail(1062L, 100L)).thenReturn(detail);
        when(placeMapper.selectPlaceImages(1062L)).thenReturn(List.of(image));
        when(placeMapper.selectPlaceTags(1062L)).thenReturn(List.of(tag));

        PlaceDetailResponse response = placeService.getPlaceDetail(1062L, 100L);

        assertThat(response.getPlaceDescription()).isEqualTo("애기봉 설명");
        assertThat(response.getIsBookmarked()).isTrue();
        assertThat(response.getPlaceImages()).extracting(PlaceImageDto::getImageUrl)
                .containsExactly("https://bucket/places/aegibong.png");
        assertThat(response.getTags()).hasSize(1);
    }

    @Test
    void 없는_장소의_상세는_404_예외를_발생시킨다() {
        when(placeMapper.selectPlaceDetail(9999L, null)).thenReturn(null);

        assertThatThrownBy(() -> placeService.getPlaceDetail(9999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 타입과_태그의_앞뒤_공백을_제거하고_조회한다() {
        when(placeMapper.findMapPlacesByType("주요관광지")).thenReturn(List.of(mapPlace()));
        when(placeMapper.findMapPlacesByTag("반려동물")).thenReturn(List.of(mapPlace()));

        assertThat(placeService.findMapPlacesByType("  주요관광지  ")).hasSize(1);
        assertThat(placeService.findMapPlacesByTag(" 반려동물 ")).hasSize(1);

        verify(placeMapper).findMapPlacesByType("주요관광지");
        verify(placeMapper).findMapPlacesByTag("반려동물");
    }

    @Test
    void 타입이나_태그가_공백이면_400_예외를_발생시킨다() {
        assertThatThrownBy(() -> placeService.findMapPlacesByType("   "))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> placeService.findMapPlacesByTag(null))
                .isInstanceOf(BadRequestException.class);
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
