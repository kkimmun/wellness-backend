package com.kh.wellness.place.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.kh.wellness.place.model.dto.MapPlaceResponse;
import com.kh.wellness.place.model.dto.PlaceTagDto;
import com.kh.wellness.place.model.dto.PlaceTypeOptionResponse;
import com.kh.wellness.place.model.service.PlaceService;

@ExtendWith(MockitoExtension.class)
class MapPlaceControllerTest {

    @Mock
    private PlaceService placeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new MapPlaceController(placeService))
                .build();
    }

    @Test
    void type_파라미터를_타입별_장소_조회에_전달한다() throws Exception {
        MapPlaceResponse response = MapPlaceResponse.builder()
                .placeNo(1L)
                .placeName("김포장릉")
                .type("주요관광지")
                .typeDetail("역사유적")
                .imageUrl("https://bucket/places/jangneung.jpg")
                .build();
        when(placeService.findMapPlacesByType("주요관광지")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/places/types")
                        .queryParam("type", "주요관광지"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("타입별 장소 조회 성공"))
                .andExpect(jsonPath("$.data[0].type").value("주요관광지"))
                .andExpect(jsonPath("$.data[0].typeDetail").value("역사유적"))
                .andExpect(jsonPath("$.data[0].imageUrl").value("https://bucket/places/jangneung.jpg"));

        verify(placeService).findMapPlacesByType("주요관광지");
    }

    @Test
    void tag_파라미터를_태그별_장소_조회에_전달한다() throws Exception {
        MapPlaceResponse response = MapPlaceResponse.builder()
                .placeNo(5L)
                .placeName("반려동물 동반 장소")
                .build();
        when(placeService.findMapPlacesByTag("반려동물")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/places/tags")
                        .queryParam("tag", "반려동물"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("태그별 장소 조회 성공"))
                .andExpect(jsonPath("$.data[0].placeNo").value(5));

        verify(placeService).findMapPlacesByTag("반려동물");
    }

    @Test
    void pins는_타입과_태그_PK를_동시에_서비스에_전달한다() throws Exception {
        when(placeService.findMapPlaces(1L, 18L, 4L)).thenReturn(List.of());

        mockMvc.perform(get("/api/places/pins")
                        .queryParam("typeNo", "1")
                        .queryParam("typeDetailNo", "18")
                        .queryParam("tagNo", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(placeService).findMapPlaces(1L, 18L, 4L);
    }

    @Test
    void 타입과_태그_선택지_API는_DB_값을_반환한다() throws Exception {
        PlaceTypeOptionResponse type = PlaceTypeOptionResponse.builder()
                .typeNo(1L)
                .type("주요관광지")
                .typeDetailNo(18L)
                .typeDetailContent("김포 TOP 10")
                .build();
        PlaceTagDto tag = new PlaceTagDto();
        tag.setTagNo(4L);
        tag.setTagContent("가족");
        when(placeService.findPlaceTypeOptions()).thenReturn(List.of(type));
        when(placeService.findPlaceTagOptions()).thenReturn(List.of(tag));

        mockMvc.perform(get("/api/places/type-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("주요관광지"))
                .andExpect(jsonPath("$.data[0].typeDetailContent").value("김포 TOP 10"));
        mockMvc.perform(get("/api/places/tag-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].tagContent").value("가족"));
    }
}
