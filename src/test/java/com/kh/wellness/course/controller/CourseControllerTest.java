package com.kh.wellness.course.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.kh.wellness.course.model.dto.CourseRestaurantResponse;
import com.kh.wellness.course.model.dto.PlaceDto;
import com.kh.wellness.course.model.service.CourseService;
import com.kh.wellness.route.model.dto.RouteSearchRequest;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {
    @Mock private CourseService courseService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() { mockMvc = MockMvcBuilders.standaloneSetup(new CourseController(courseService)).build(); }

    @Test
    void returnsRestaurantDetailsAndDistanceInExistingApiEnvelope() throws Exception {
        PlaceDto place = PlaceDto.builder().placeNo(6L).placeName("경로 옆 식당")
                .imageUrl("restaurant.jpg").addr("김포시").xAxis(126.7).yAxis(37.6).build();
        when(courseService.getRestaurants(any(RouteSearchRequest.class)))
                .thenReturn(List.of(new CourseRestaurantResponse(place, 350.25)));
        mockMvc.perform(post("/api/courses/restaurants").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"startPlaceNo":10,"endPlaceNo":20,"transportType":"WALK"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].place.placeNo").value(6))
                .andExpect(jsonPath("$.data[0].place.imageUrl").value("restaurant.jpg"))
                .andExpect(jsonPath("$.data[0].distance").value(350.25));
    }

    @Test
    void returnsEmptyListWhenNoRestaurantIsNearby() throws Exception {
        when(courseService.getRestaurants(any(RouteSearchRequest.class))).thenReturn(List.of());
        mockMvc.perform(post("/api/courses/restaurants").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"startX":126.7,"startY":37.6,"endPlaceNo":20,"transportType":"WALK"}
                                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void rejectsInvalidCoordinatesAndPlaceNumbersBeforeCallingService() throws Exception {
        for (String body : List.of(
                "{\"startPlaceNo\":-1,\"endPlaceNo\":20,\"transportType\":\"WALK\"}",
                "{\"startX\":181,\"startY\":37,\"endPlaceNo\":20,\"transportType\":\"WALK\"}",
                "{\"startPlaceNo\":10,\"endPlaceNo\":20}")) {
            mockMvc.perform(post("/api/courses/restaurants").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(courseService);
    }
}
