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
}
