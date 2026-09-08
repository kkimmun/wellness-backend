package com.kh.wellness.place.controller;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.kh.wellness.configuration.SecurityConfiguration;
import com.kh.wellness.configuration.filter.JwtFilter;
import com.kh.wellness.place.model.dto.MapPlaceResponse;
import com.kh.wellness.place.model.dto.PlaceDetailResponse;
import com.kh.wellness.place.model.dto.PlaceImageDto;
import com.kh.wellness.place.model.dto.PlaceImageLicenseDto;
import com.kh.wellness.place.model.dto.PlaceTagDto;
import com.kh.wellness.place.model.dto.PlaceTypeOptionResponse;
import com.kh.wellness.place.model.service.PlaceService;
import com.kh.wellness.token.util.JwtUtil;

@SpringJUnitConfig(PlaceControllerTest.Config.class)
@WebAppConfiguration
class PlaceControllerTest {
    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({PlaceController.class, SecurityConfiguration.class})
    static class Config {
        @Bean PlaceService placeService() { return mock(PlaceService.class); }
        @Bean JwtFilter jwtFilter() { return new JwtFilter(mock(JwtUtil.class)); }
    }

    @Autowired WebApplicationContext context;
    @Autowired PlaceService placeService;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        reset(placeService);
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void anonymousDetailRequestUsesDetailPathWithoutCategoryCollision() throws Exception {
        PlaceImageLicenseDto license = new PlaceImageLicenseDto();
        license.setSourceName("김포시");
        license.setSourcePageUrl("https://example.com/source");
        license.setAuthorName("김포시 문화예술과");
        license.setLicenseCode("KOGL TYPE1");
        license.setLicenseUrl("https://www.kogl.or.kr/info/licenseType1.do");
        license.setAttributionText("사진: 김포시, 공공누리 제1유형");
        PlaceImageDto image = new PlaceImageDto();
        image.setImageUrl("https://bucket/places/temple.jpg");
        image.setLicense(license);
        when(placeService.getPlaceDetail(106L, null)).thenReturn(
                PlaceDetailResponse.builder()
                        .placeNo(106L)
                        .placeName("금정사")
                        .placeImages(List.of(image))
                        .build());
        mvc.perform(get("/api/places/106/detail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.placeNo").value(106))
                .andExpect(jsonPath("$.data.placeImages[0].license.sourceName").value("김포시"))
                .andExpect(jsonPath("$.data.placeImages[0].license.licenseCode").value("KOGL TYPE1"))
                .andExpect(jsonPath("$.data.placeImages[0].license.attributionText")
                        .value("사진: 김포시, 공공누리 제1유형"));
        verify(placeService).getPlaceDetail(106L, null);
        verify(placeService, never()).selectPlaces(anyLong());
    }

    @Test
    void anonymousCategoryRequestUsesPluralPlacesPath() throws Exception {
        when(placeService.selectPlaces(9L)).thenReturn(List.of());
        mvc.perform(get("/api/places/9"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").isArray());
        verify(placeService).selectPlaces(9L);
        verify(placeService, never()).getPlaceDetail(anyLong(), any());
    }

    @Test
    void pinsRemainAccessibleWithoutAuthentication() throws Exception {
        when(placeService.findMapPlaces()).thenReturn(List.of());
        mvc.perform(get("/api/places/pins"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").isArray());
        verify(placeService).findMapPlaces();
    }

    @Test
    void pinsPassTypeAndTagFiltersToService() throws Exception {
        when(placeService.findMapPlaces(1L, 18L, 4L)).thenReturn(List.of());

        mvc.perform(get("/api/places/pins")
                        .queryParam("typeNo", "1")
                        .queryParam("typeDetailNo", "18")
                        .queryParam("tagNo", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(placeService).findMapPlaces(1L, 18L, 4L);
    }

    @Test
    void typeAndTagQueriesReturnMapPlaces() throws Exception {
        MapPlaceResponse typePlace = MapPlaceResponse.builder()
                .placeNo(1L)
                .placeName("김포장릉")
                .type("주요관광지")
                .build();
        MapPlaceResponse tagPlace = MapPlaceResponse.builder()
                .placeNo(2L)
                .placeName("반려동물 동반 장소")
                .build();
        when(placeService.findMapPlacesByType("주요관광지")).thenReturn(List.of(typePlace));
        when(placeService.findMapPlacesByTag("반려동물")).thenReturn(List.of(tagPlace));

        mvc.perform(get("/api/places/types").queryParam("type", "주요관광지"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].placeNo").value(1));
        mvc.perform(get("/api/places/tags").queryParam("tag", "반려동물"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].placeNo").value(2));

        verify(placeService).findMapPlacesByType("주요관광지");
        verify(placeService).findMapPlacesByTag("반려동물");
    }

    @Test
    void typeAndTagOptionsReturnDatabaseValues() throws Exception {
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

        mvc.perform(get("/api/places/type-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].typeDetailContent").value("김포 TOP 10"));
        mvc.perform(get("/api/places/tag-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].tagContent").value("가족"));
    }

    @Test
    void publicCategoryAccessDoesNotExposeReviewWrites() throws Exception {
        mvc.perform(post("/api/places/106/reviews")).andExpect(status().isForbidden());
        mvc.perform(delete("/api/places/106/reviews/1")).andExpect(status().isForbidden());
        verifyNoInteractions(placeService);
    }
}
