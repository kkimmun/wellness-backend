package com.kh.wellness.route.model.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.exception.InternalServerException;
import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.route.model.dao.RouteMapper;
import com.kh.wellness.route.model.dto.RouteResponse;
import com.kh.wellness.route.model.dto.RouteSearchRequest;
import com.kh.wellness.route.model.vo.Place;
import com.kh.wellness.route.model.vo.TransportType;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private RouteMapper routeMapper;

    @Mock
    private KakaoRouteClient kakaoRouteClient;

    private RouteService routeService;
    private ObjectMapper objectMapper;
    private Place origin;
    private Place destination;

    @BeforeEach
    void setUp() {
        routeService = new RouteService(routeMapper, kakaoRouteClient);
        objectMapper = new ObjectMapper();
        origin = Place.builder()
                .placeNo(1L)
                .placeName("김포국제공항")
                .xAxis(126.9)
                .yAxis(37.5)
                .build();
        destination = Place.builder()
                .placeNo(10L)
                .placeName("김포아트빌리지")
                .xAxis(127.1)
                .yAxis(37.6)
                .build();
    }

    @Test
    void 자동차_최소거리_옵션을_카카오_요청으로_변환하고_응답을_가공한다() throws Exception {
        RouteSearchRequest request = request("CAR", "MIN_DISTANCE");
        stubRoutePlaces();
        when(kakaoRouteClient.findCarRoute(126.9, 37.5, 127.1, 37.6, "DISTANCE", null))
                .thenReturn(json("""
                        {
                          "routes": [{
                            "result_code": 0,
                            "summary": {
                              "distance": 1200,
                              "duration": 300,
                              "fare": {"toll": 0}
                            },
                            "sections": [{
                              "roads": [{"vertexes": [126.9, 37.5, 127.0, 37.55]}],
                              "guides": [{
                                "type": 1,
                                "guidance": "직진",
                                "distance": 100,
                                "duration": 20,
                                "x": 126.9,
                                "y": 37.5
                              }]
                            }]
                          }]
                        }
                        """));

        RouteResponse response = routeService.findRoutes(request);

        assertThat(response.getTransportType()).isEqualTo(TransportType.CAR);
        assertThat(response.getSelectedOption()).isEqualTo("MIN_DISTANCE");
        assertThat(response.getOrigin().getPlaceNo()).isEqualTo(1L);
        assertThat(response.getDestination().getPlaceNo()).isEqualTo(10L);
        assertThat(response.getRoutes()).hasSize(1);
        assertThat(response.getRoutes().getFirst().getTotalDistance()).isEqualTo(1200);
        assertThat(response.getRoutes().getFirst().getPath()).hasSize(2);
        assertThat(response.getRoutes().getFirst().getSteps()).hasSize(1);
    }

    @Test
    void 자동차_유료도로_회피_옵션은_avoid_toll로_요청한다() throws Exception {
        RouteSearchRequest request = request("CAR", "AVOID_TOLL");
        stubRoutePlaces();
        when(kakaoRouteClient.findCarRoute(126.9, 37.5, 127.1, 37.6, "RECOMMEND", "toll"))
                .thenReturn(json("""
                        {
                          "routes": [{
                            "result_code": 0,
                            "summary": {"distance": 1000, "duration": 400, "fare": {"toll": 0}},
                            "sections": []
                          }]
                        }
                        """));

        RouteResponse response = routeService.findRoutes(request);

        assertThat(response.getSelectedOption()).isEqualTo("AVOID_TOLL");
        verify(kakaoRouteClient)
                .findCarRoute(126.9, 37.5, 127.1, 37.6, "RECOMMEND", "toll");
    }

    @Test
    void 대중교통은_그룹을_나누고_최소도보_후_소요시간으로_정렬한다() throws Exception {
        RouteSearchRequest request = request("PUBLIC_TRANSIT", null);
        request.setSortType("MIN_WALK");
        stubRoutePlaces();
        when(kakaoRouteClient.findPublicTransitRoutes(126.9, 37.5, 127.1, 37.6))
                .thenReturn(json("""
                        {
                          "status": "OK",
                          "routes": [
                            {
                              "properties": {
                                "type": "BUS",
                                "totalDistance": 5000,
                                "totalTime": 500,
                                "transfers": 0,
                                "fare": {"value": 1500}
                              },
                              "steps": [{
                                "properties": {
                                  "type": "WALKING",
                                  "guidance": "200m 걷기",
                                  "distance": 200,
                                  "time": 180,
                                  "stops": [],
                                  "vehicles": []
                                },
                                "path": {"points": [[126.9, 37.5]]}
                              }]
                            },
                            {
                              "properties": {
                                "type": "SUBWAY",
                                "totalDistance": 6000,
                                "totalTime": 700,
                                "transfers": 1,
                                "fare": {"value": 1650}
                              },
                              "steps": []
                            },
                            {
                              "properties": {
                                "type": "BUS",
                                "totalDistance": 5200,
                                "totalTime": 600,
                                "transfers": 1,
                                "fare": {"value": 1500}
                              },
                              "steps": [{
                                "properties": {
                                  "type": "WALKING",
                                  "guidance": "100m 걷기",
                                  "distance": 100,
                                  "time": 90,
                                  "stops": [],
                                  "vehicles": []
                                },
                                "path": {"points": [[126.9, 37.5]]}
                              }]
                            }
                          ]
                        }
                        """));

        RouteResponse response = routeService.findRoutes(request);

        assertThat(response.getRoutes())
                .extracting(route -> route.getRouteType())
                .containsExactly("SUBWAY", "BUS", "BUS");
        assertThat(response.getRoutes().get(1).getWalkingDistance()).isEqualTo(100);
        assertThat(response.getRoutes().get(2).getWalkingDistance()).isEqualTo(200);
    }

    @Test
    void 자전거_최소시간은_제공되는_탐색방식의_totalTime을_비교한다() throws Exception {
        RouteSearchRequest request = request("BICYCLE", "MIN_TIME");
        stubRoutePlaces();
        when(kakaoRouteClient.findBicycleRoute(126.9, 37.5, 127.1, 37.6, "BIKE_ONLY"))
                .thenReturn(simpleRoute(900));
        when(kakaoRouteClient.findBicycleRoute(126.9, 37.5, 127.1, 37.6, "SHORTEST"))
                .thenReturn(simpleRoute(600));
        when(kakaoRouteClient.findBicycleRoute(126.9, 37.5, 127.1, 37.6, "ACCESSIBLE"))
                .thenReturn(simpleRoute(700));

        RouteResponse response = routeService.findRoutes(request);

        assertThat(response.getRoutes()).hasSize(1);
        assertThat(response.getRoutes().getFirst().getTotalTime()).isEqualTo(600);
        verify(kakaoRouteClient)
                .findBicycleRoute(126.9, 37.5, 127.1, 37.6, "BIKE_ONLY");
        verify(kakaoRouteClient)
                .findBicycleRoute(126.9, 37.5, 127.1, 37.6, "SHORTEST");
        verify(kakaoRouteClient)
                .findBicycleRoute(126.9, 37.5, 127.1, 37.6, "ACCESSIBLE");
    }

    @Test
    void 출발장소가_없으면_외부_API를_호출하지_않는다() {
        RouteSearchRequest request = request("WALK", "SHORTEST");

        assertThatThrownBy(() -> routeService.findRoutes(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("출발 장소를 찾을 수 없습니다.");

        verifyNoInteractions(kakaoRouteClient);
    }

    @Test
    void 출발지와_도착지가_같으면_외부_API를_호출하지_않는다() {
        RouteSearchRequest request = request("WALK", "SHORTEST");
        request.setStartPlaceNo(10L);
        when(routeMapper.findPlaceByNo(10L)).thenReturn(destination);

        assertThatThrownBy(() -> routeService.findRoutes(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("출발지와 도착지가 같습니다.");

        verifyNoInteractions(kakaoRouteClient);
    }

    @Test
    void 현재좌표에서_DB장소로_길찾기할_수_있다() throws Exception {
        RouteSearchRequest request = request("WALK", "SHORTEST");
        request.setStartPlaceNo(null);
        request.setStartX(126.8);
        request.setStartY(37.55);
        when(routeMapper.findPlaceByNo(10L)).thenReturn(destination);
        when(kakaoRouteClient.findWalkingRoute(
                126.8, 37.55, 127.1, 37.6, "SHORTEST", List.of()))
                .thenReturn(simpleRoute(600));

        RouteResponse response = routeService.findRoutes(request);

        assertThat(response.getOrigin().getPlaceNo()).isNull();
        assertThat(response.getOrigin().getPlaceName()).isEqualTo("현재 위치");
        assertThat(response.getDestination().getPlaceNo()).isEqualTo(10L);
        verify(routeMapper, never()).findPlaceByNo(1L);
    }

    @Test
    void DB장소에서_임의좌표로_길찾기할_수_있다() throws Exception {
        RouteSearchRequest request = request("WALK", "SHORTEST");
        request.setEndPlaceNo(null);
        request.setEndX(126.8);
        request.setEndY(37.55);
        when(routeMapper.findPlaceByNo(1L)).thenReturn(origin);
        when(kakaoRouteClient.findWalkingRoute(
                126.9, 37.5, 126.8, 37.55, "SHORTEST", List.of()))
                .thenReturn(simpleRoute(600));

        RouteResponse response = routeService.findRoutes(request);

        assertThat(response.getOrigin().getPlaceNo()).isEqualTo(1L);
        assertThat(response.getDestination().getPlaceNo()).isNull();
        assertThat(response.getDestination().getPlaceName()).isEqualTo("지정 도착지");
        verify(routeMapper, never()).findPlaceByNo(10L);
    }

    @Test
    void 임의좌표_사이도_길찾기할_수_있다() throws Exception {
        RouteSearchRequest request = request("WALK", "SHORTEST");
        request.setStartPlaceNo(null);
        request.setStartX(126.7);
        request.setStartY(37.6);
        request.setEndPlaceNo(null);
        request.setEndX(126.8);
        request.setEndY(37.55);
        when(kakaoRouteClient.findWalkingRoute(
                126.7, 37.6, 126.8, 37.55, "SHORTEST", List.of()))
                .thenReturn(simpleRoute(600));

        RouteResponse response = routeService.findRoutes(request);

        assertThat(response.getOrigin().getPlaceName()).isEqualTo("현재 위치");
        assertThat(response.getDestination().getPlaceName()).isEqualTo("지정 도착지");
        verifyNoInteractions(routeMapper);
    }

    @Test
    void 도보_경유지는_입력된_순서대로_카카오_API에_전달한다() throws Exception {
        RouteSearchRequest request = request("WALK", "SHORTEST");
        request.setWaypointPlaceNos(List.of(15L, 16L));
        Place firstWaypoint = Place.builder()
                .placeNo(15L)
                .placeName("첫 번째 경유지")
                .xAxis(126.93)
                .yAxis(37.53)
                .build();
        Place secondWaypoint = Place.builder()
                .placeNo(16L)
                .placeName("두 번째 경유지")
                .xAxis(126.95)
                .yAxis(37.55)
                .build();
        stubRoutePlaces();
        when(routeMapper.findPlaceByNo(15L)).thenReturn(firstWaypoint);
        when(routeMapper.findPlaceByNo(16L)).thenReturn(secondWaypoint);
        when(kakaoRouteClient.findWalkingRoute(
                eq(126.9),
                eq(37.5),
                eq(127.1),
                eq(37.6),
                eq("SHORTEST"),
                anyList()
        )).thenReturn(simpleRoute(600));

        RouteResponse response = routeService.findRoutes(request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Place>> captor = ArgumentCaptor.forClass(List.class);
        verify(kakaoRouteClient).findWalkingRoute(
                eq(126.9),
                eq(37.5),
                eq(127.1),
                eq(37.6),
                eq("SHORTEST"),
                captor.capture()
        );
        assertThat(captor.getValue())
                .extracting(Place::getXAxis)
                .containsExactly(126.93, 126.95);
        assertThat(response.getWaypoints())
                .extracting(waypoint -> waypoint.getPlaceName())
                .containsExactly("첫 번째 경유지", "두 번째 경유지");
    }

    @Test
    void 경유지가_4개면_거부한다() {
        RouteSearchRequest request = request("WALK", "SHORTEST");
        request.setWaypointPlaceNos(List.of(11L, 12L, 13L, 14L));
        stubRoutePlaces();

        assertThatThrownBy(() -> routeService.findRoutes(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("경유지는 최대 3개까지 설정할 수 있습니다.");

        verifyNoInteractions(kakaoRouteClient);
    }

    @Test
    void 도보가_아닌_길찾기에서_경유지를_보내면_거부한다() {
        RouteSearchRequest request = request("CAR", "MIN_DISTANCE");
        request.setWaypointPlaceNos(List.of(15L));
        stubRoutePlaces();

        assertThatThrownBy(() -> routeService.findRoutes(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("경유지는 도보 길찾기에서만 사용할 수 있습니다.");

        verifyNoInteractions(kakaoRouteClient);
    }

    @Test
    void DB장소번호가_없는_경유지는_거부한다() {
        RouteSearchRequest request = request("WALK", "SHORTEST");
        request.setWaypointPlaceNos(java.util.Collections.singletonList(null));
        stubRoutePlaces();

        assertThatThrownBy(() -> routeService.findRoutes(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("경유지는 DB 장소 번호로만 설정할 수 있습니다.");

        verifyNoInteractions(kakaoRouteClient);
    }

    @Test
    void 장소번호와_좌표를_동시에_보내면_거부한다() {
        RouteSearchRequest request = request("CAR", "MIN_DISTANCE");
        request.setStartX(126.8);
        request.setStartY(37.55);

        assertThatThrownBy(() -> routeService.findRoutes(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("출발 장소 번호와 좌표 중 하나만 입력해야 합니다.");

        verifyNoInteractions(routeMapper, kakaoRouteClient);
    }

    @Test
    void 도착장소가_없으면_외부_API를_호출하지_않는다() {
        RouteSearchRequest request = request("WALK", "SHORTEST");
        when(routeMapper.findPlaceByNo(1L)).thenReturn(origin);

        assertThatThrownBy(() -> routeService.findRoutes(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("도착 장소를 찾을 수 없습니다.");

        verify(kakaoRouteClient, never())
                .findWalkingRoute(126.9, 37.5, 127.1, 37.6, "SHORTEST", List.of());
    }

    @Test
    void 도착장소_좌표가_없으면_외부_API를_호출하지_않는다() {
        RouteSearchRequest request = request("WALK", "SHORTEST");
        destination.setYAxis(null);
        when(routeMapper.findPlaceByNo(1L)).thenReturn(origin);
        when(routeMapper.findPlaceByNo(10L)).thenReturn(destination);

        assertThatThrownBy(() -> routeService.findRoutes(request))
                .isInstanceOf(InternalServerException.class)
                .hasMessage("도착 장소의 좌표 정보가 없습니다.");

        verify(kakaoRouteClient, never())
                .findWalkingRoute(126.9, 37.5, 127.1, 37.6, "SHORTEST", List.of());
    }

    @Test
    void 출발지_검색은_DB에_등록된_장소만_응답으로_변환한다() {
        Place place = Place.builder()
                .placeNo(15L)
                .placeName("김포 장릉")
                .address("경기 김포시 장릉로 79")
                .xAxis(126.7107)
                .yAxis(37.6124)
                .build();
        when(routeMapper.findPlacesByQuery("장릉")).thenReturn(List.of(place));

        var origins = routeService.searchOrigins(" 장릉 ");

        assertThat(origins).hasSize(1);
        assertThat(origins.getFirst().getPlaceNo()).isEqualTo(15L);
        assertThat(origins.getFirst().getPlaceName()).isEqualTo("김포 장릉");
        assertThat(origins.getFirst().getAddress()).isEqualTo("경기 김포시 장릉로 79");
        assertThat(origins.getFirst().getXAxis()).isEqualTo(126.7107);
        verify(routeMapper).findPlacesByQuery("장릉");
        verifyNoInteractions(kakaoRouteClient);
    }

    @Test
    void 빈_출발지_검색어는_거부한다() {
        assertThatThrownBy(() -> routeService.searchOrigins("  "))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("검색어는 필수입니다.");
    }

    private RouteSearchRequest request(String transportType, String routeOption) {
        RouteSearchRequest request = new RouteSearchRequest();
        request.setEndPlaceNo(10L);
        request.setStartPlaceNo(1L);
        request.setTransportType(transportType);
        request.setRouteOption(routeOption);
        return request;
    }

    private void stubRoutePlaces() {
        when(routeMapper.findPlaceByNo(1L)).thenReturn(origin);
        when(routeMapper.findPlaceByNo(10L)).thenReturn(destination);
    }

    private JsonNode simpleRoute(int totalTime) throws Exception {
        return json("""
                {
                  "status": "OK",
                  "route": {
                    "properties": {
                      "totalDistance": 3000,
                      "totalTime": %d,
                      "landingUrl": "https://map.kakao.com/route"
                    },
                    "legs": []
                  }
                }
                """.formatted(totalTime));
    }

    private JsonNode json(String value) throws Exception {
        return objectMapper.readTree(value);
    }
}
