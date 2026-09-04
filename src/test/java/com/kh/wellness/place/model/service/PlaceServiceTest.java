package com.kh.wellness.place.model.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.place.model.dao.PlaceMapper;
import com.kh.wellness.place.model.dto.PlaceDetailDto;
import com.kh.wellness.place.model.dto.PlaceDetailResponse;
import com.kh.wellness.place.model.dto.PlaceImageDto;
import com.kh.wellness.place.model.dto.PlaceTagDto;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

    @Mock
    private PlaceMapper placeMapper;

    @InjectMocks
    private PlaceService placeService;

    @Test
    @DisplayName("[우리 관리 장소] 우리 DB에 존재하는 장소인 경우 정상적으로 상세 정보를 반환한다")
    void getPlaceDetail_ourPlace_success() {
        PlaceDetailDto detailDto = new PlaceDetailDto();
        detailDto.setPlaceNo(1L);
        detailDto.setPlaceName("김포 장릉");
        detailDto.setBookmarkYn("Y");
        
        when(placeMapper.selectPlaceDetail(1L, 100L)).thenReturn(detailDto);
        when(placeMapper.selectPlaceImages(1L)).thenReturn(List.of(new PlaceImageDto()));
        when(placeMapper.selectPlaceTags(1L)).thenReturn(List.of(new PlaceTagDto()));

        PlaceDetailResponse response = placeService.getPlaceDetail(1L, 100L);

        assertThat(response).isNotNull();
        assertThat(response.getPlaceName()).isEqualTo("김포 장릉");
        assertThat(response.getIsBookmarked()).isTrue();
        assertThat(response.getPlaceImages()).hasSize(1);
        assertThat(response.getTags()).hasSize(1);
    }



    @Test
    @DisplayName("[미등록 일반 장소] DB와 카카오 API 모두에 없는 장소면 NotFoundException 예외가 발생한다")
    void getPlaceDetail_notFound() {
        // 참고: 프론트에서 카카오 연동을 통째로 넘기기로 기획이 바뀌기 이전의 원래 요구사항을 반영한 테스트입니다.
        when(placeMapper.selectPlaceDetail(999L, 100L)).thenReturn(null);

        // 현재 구현에서는 DB에 없으면 무조건 NotFoundException을 던지므로 항상 성공합니다.
        assertThatThrownBy(() -> placeService.getPlaceDetail(999L, 100L))
                .isInstanceOf(NotFoundException.class);
    }
}
