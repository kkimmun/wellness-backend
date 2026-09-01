package com.kh.wellness.route.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.kh.wellness.route.model.dto.OriginSearchResponse;
import com.kh.wellness.route.model.dto.PlaceResponse;
import com.kh.wellness.route.model.dto.RouteResponse;
import com.kh.wellness.route.model.dto.RouteSearchRequest;
import com.kh.wellness.route.model.service.RouteService;
import com.kh.wellness.route.model.vo.TransportType;

@ExtendWith(MockitoExtension.class)
class RouteControllerTest {

    @Mock
    private RouteService routeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new RouteController(routeService))
                .build();
    }

    @Test
    void query_요청_파라미터를_출발지_검색어로_전달한다() throws Exception {
        OriginSearchResponse response = OriginSearchResponse.builder()
                .placeNo(7L)
                .placeName("김포국제공항")
                .address("서울 강서구 하늘길 38")
                .xAxis(126.8014)
                .yAxis(37.5587)
                .build();
        when(routeService.searchOrigins("김포공항")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/routes/origins")
                        .queryParam("query", "김포공항"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("출발지 검색 성공"))
                .andExpect(jsonPath("$.data[0].placeNo").value(7))
                .andExpect(jsonPath("$.data[0].placeName").value("김포국제공항"))
                .andExpect(jsonPath("$.data[0].X_AXIS").value(126.8014))
                .andExpect(jsonPath("$.data[0].Y_AXIS").value(37.5587))
                .andExpect(jsonPath("$.data[0].XAxis").doesNotExist())
                .andExpect(jsonPath("$.data[0].YAxis").doesNotExist());

        verify(routeService).searchOrigins("김포공항");
    }

    @Test
    void 출발지와_도착지_장소번호를_길찾기_요청으로_전달한다() throws Exception {
        RouteResponse response = RouteResponse.builder()
                .transportType(TransportType.CAR)
                .selectedOption("MIN_DISTANCE")
                .origin(PlaceResponse.builder().placeNo(248L).build())
                .destination(PlaceResponse.builder().placeNo(7L).build())
                .routes(List.of())
                .build();
        when(routeService.findRoutes(any(RouteSearchRequest.class))).thenReturn(response);

        mockMvc.perform(get("/api/routes")
                        .queryParam("startPlaceNo", "248")
                        .queryParam("endPlaceNo", "7")
                        .queryParam("transportType", "CAR")
                        .queryParam("routeOption", "MIN_DISTANCE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.origin.placeNo").value(248))
                .andExpect(jsonPath("$.data.destination.placeNo").value(7));

        ArgumentCaptor<RouteSearchRequest> captor =
                ArgumentCaptor.forClass(RouteSearchRequest.class);
        verify(routeService).findRoutes(captor.capture());
        assertThat(captor.getValue().getStartPlaceNo()).isEqualTo(248L);
        assertThat(captor.getValue().getEndPlaceNo()).isEqualTo(7L);
    }

    @Test
    void 현재위치_좌표와_DB도착지를_길찾기_요청으로_전달한다() throws Exception {
        RouteResponse response = RouteResponse.builder()
                .transportType(TransportType.CAR)
                .selectedOption("MIN_DISTANCE")
                .origin(PlaceResponse.builder()
                        .xAxis(126.8027)
                        .yAxis(37.5586)
                        .build())
                .destination(PlaceResponse.builder().placeNo(7L).build())
                .routes(List.of())
                .build();
        when(routeService.findRoutes(any(RouteSearchRequest.class))).thenReturn(response);

        mockMvc.perform(get("/api/routes")
                        .queryParam("startX", "126.8027")
                        .queryParam("startY", "37.5586")
                        .queryParam("endPlaceNo", "7")
                        .queryParam("transportType", "CAR")
                        .queryParam("routeOption", "MIN_DISTANCE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.origin.X_AXIS").value(126.8027))
                .andExpect(jsonPath("$.data.origin.Y_AXIS").value(37.5586))
                .andExpect(jsonPath("$.data.origin.XAxis").doesNotExist())
                .andExpect(jsonPath("$.data.origin.YAxis").doesNotExist())
                .andExpect(jsonPath("$.data.destination.placeNo").value(7));

        ArgumentCaptor<RouteSearchRequest> captor =
                ArgumentCaptor.forClass(RouteSearchRequest.class);
        verify(routeService).findRoutes(captor.capture());
        assertThat(captor.getValue().getStartPlaceNo()).isNull();
        assertThat(captor.getValue().getStartX()).isEqualTo(126.8027);
        assertThat(captor.getValue().getStartY()).isEqualTo(37.5586);
        assertThat(captor.getValue().getEndPlaceNo()).isEqualTo(7L);
    }

    @Test
    void 인덱스로_입력한_경유지의_순서를_유지한다() throws Exception {
        RouteResponse response = RouteResponse.builder()
                .transportType(TransportType.WALK)
                .selectedOption("SHORTEST")
                .origin(PlaceResponse.builder().placeNo(248L).build())
                .destination(PlaceResponse.builder().placeNo(7L).build())
                .waypoints(List.of(
                        PlaceResponse.builder().placeNo(15L).build(),
                        PlaceResponse.builder().placeNo(16L).build()
                ))
                .routes(List.of())
                .build();
        when(routeService.findRoutes(any(RouteSearchRequest.class))).thenReturn(response);

        mockMvc.perform(get("/api/routes")
                        .queryParam("startPlaceNo", "248")
                        .queryParam("endPlaceNo", "7")
                        .queryParam("transportType", "WALK")
                        .queryParam("routeOption", "SHORTEST")
                        .queryParam("waypointPlaceNos", "15", "16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.waypoints[0].placeNo").value(15))
                .andExpect(jsonPath("$.data.waypoints[1].placeNo").value(16));

        ArgumentCaptor<RouteSearchRequest> captor =
                ArgumentCaptor.forClass(RouteSearchRequest.class);
        verify(routeService).findRoutes(captor.capture());
        assertThat(captor.getValue().getWaypointPlaceNos()).containsExactly(15L, 16L);
    }

    @Test
    void 올바르지_않은_DB경유지번호는_거부한다() throws Exception {
        mockMvc.perform(get("/api/routes")
                        .queryParam("startPlaceNo", "248")
                        .queryParam("endPlaceNo", "7")
                        .queryParam("transportType", "WALK")
                        .queryParam("routeOption", "SHORTEST")
                        .queryParam("waypointPlaceNos", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(routeService);
    }
}
