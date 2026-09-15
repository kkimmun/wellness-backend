package com.kh.wellness.route.model.dto;

import java.util.List;

import com.kh.wellness.route.model.vo.TransportType;

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
public class RouteResponse {

    private TransportType transportType;
    private String selectedOption;
    private RoutePlaceResponse origin;
    private RoutePlaceResponse destination;
    private List<RoutePlaceResponse> waypoints;
    private List<RouteResultResponse> routes;
}
