package com.kh.wellness.route.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RouteResultResponse {

    private String routeType;
    private Integer totalDistance;
    private Integer totalTime;
    private Integer transfers;
    private Integer walkingDistance;
    private Integer fare;
    private Integer toll;
    private String landingUrl;
    private List<RouteStepResponse> steps;
    private List<CoordinateResponse> path;
}
