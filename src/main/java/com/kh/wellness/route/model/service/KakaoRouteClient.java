package com.kh.wellness.route.model.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;

import com.kh.wellness.exception.InternalServerException;
import com.kh.wellness.route.model.vo.Place;

import tools.jackson.databind.JsonNode;

@Component
public class KakaoRouteClient {

    private static final String KAKAO_MAP_BASE_URL = "https://dapi.kakao.com";
    private static final String KAKAO_MOBILITY_BASE_URL = "https://apis-navi.kakaomobility.com";

    private final RestClient kakaoMapClient;
    private final RestClient kakaoMobilityClient;

    public KakaoRouteClient(@Value("${KAKAO_REST_API_KEY}") String restApiKey) {
        String authorization = "KakaoAK " + restApiKey;

        this.kakaoMapClient = RestClient.builder()
                .baseUrl(KAKAO_MAP_BASE_URL)
                .defaultHeader("Authorization", authorization)
                .build();
        this.kakaoMobilityClient = RestClient.builder()
                .baseUrl(KAKAO_MOBILITY_BASE_URL)
                .defaultHeader("Authorization", authorization)
                .build();
    }

    public JsonNode findCarRoute(
            double startX,
            double startY,
            double endX,
            double endY,
            String priority,
            String avoid) {
        return retrieve(kakaoMobilityClient.get()
                .uri(builder -> {
                    UriBuilder uriBuilder = builder
                            .path("/v1/directions")
                            .queryParam("origin", coordinate(startX, startY))
                            .queryParam("destination", coordinate(endX, endY))
                            .queryParam("priority", priority)
                            .queryParam("summary", false);

                    if (avoid != null) {
                        uriBuilder.queryParam("avoid", avoid);
                    }

                    return uriBuilder.build();
                }));
    }

    public JsonNode findPublicTransitRoutes(
            double startX,
            double startY,
            double endX,
            double endY) {
        return retrieve(kakaoMapClient.get()
                .uri(builder -> builder
                        .path("/v2/routing/publictraffic")
                        .queryParam("start_x", startX)
                        .queryParam("start_y", startY)
                        .queryParam("end_x", endX)
                        .queryParam("end_y", endY)
                        .build()));
    }

    public JsonNode findWalkingRoute(
            double startX,
            double startY,
            double endX,
            double endY,
            String routeMode,
            List<Place> waypoints) {
        return retrieve(kakaoMapClient.get()
                .uri(builder -> {
                    UriBuilder uriBuilder = builder
                            .path("/v2/routing/walk")
                            .queryParam("start_x", startX)
                            .queryParam("start_y", startY)
                            .queryParam("end_x", endX)
                            .queryParam("end_y", endY)
                            .queryParam("route_mode", routeMode);

                    if (waypoints != null && !waypoints.isEmpty()) {
                        uriBuilder
                                .queryParam("via_x", joinXAxis(waypoints))
                                .queryParam("via_y", joinYAxis(waypoints));
                    }

                    return uriBuilder.build();
                }));
    }

    public JsonNode findBicycleRoute(
            double startX,
            double startY,
            double endX,
            double endY,
            String routeMode) {
        return findKakaoMapRoute(
                "/v2/routing/bicycle",
                startX,
                startY,
                endX,
                endY,
                routeMode
        );
    }

    private JsonNode findKakaoMapRoute(
            String path,
            double startX,
            double startY,
            double endX,
            double endY,
            String routeMode) {
        return retrieve(kakaoMapClient.get()
                .uri(builder -> builder
                        .path(path)
                        .queryParam("start_x", startX)
                        .queryParam("start_y", startY)
                        .queryParam("end_x", endX)
                        .queryParam("end_y", endY)
                        .queryParam("route_mode", routeMode)
                        .build()));
    }

    private JsonNode retrieve(RestClient.RequestHeadersSpec<?> request) {
        try {
            JsonNode response = request.retrieve().body(JsonNode.class);
            if (response == null) {
                throw new InternalServerException("카카오 API 응답이 비어 있습니다.");
            }
            return response;
        } catch (RestClientException exception) {
            throw new InternalServerException("카카오 API 호출에 실패했습니다.", exception);
        }
    }

    private String coordinate(double xAxis, double yAxis) {
        return xAxis + "," + yAxis;
    }

    private String joinXAxis(List<Place> waypoints) {
        return waypoints.stream()
                .map(place -> String.valueOf(place.getXAxis()))
                .collect(Collectors.joining(","));
    }

    private String joinYAxis(List<Place> waypoints) {
        return waypoints.stream()
                .map(place -> String.valueOf(place.getYAxis()))
                .collect(Collectors.joining(","));
    }
}
