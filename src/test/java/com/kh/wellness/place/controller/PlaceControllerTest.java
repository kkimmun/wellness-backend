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
import com.kh.wellness.place.model.dto.PlaceDetailResponse;
import com.kh.wellness.place.model.service.PlaceService;
import com.kh.wellness.token.util.JwtUtil;

@SpringJUnitConfig(PlaceControllerTest.Config.class)
@WebAppConfiguration
class PlaceControllerTest {
    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({PlaceController.class, MapPlaceController.class, SecurityConfiguration.class})
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
    void anonymousDetailRequestUsesSingularPlacePathWithoutCategoryCollision() throws Exception {
        when(placeService.getPlaceDetail(106L, null)).thenReturn(
                PlaceDetailResponse.builder().placeNo(106L).placeName("금정사").build());
        mvc.perform(get("/api/place/106"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.placeNo").value(106));
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
    void publicCategoryAccessDoesNotExposeReviewWrites() throws Exception {
        mvc.perform(post("/api/places/106/reviews")).andExpect(status().isForbidden());
        mvc.perform(delete("/api/places/106/reviews/1")).andExpect(status().isForbidden());
        verifyNoInteractions(placeService);
    }
}
