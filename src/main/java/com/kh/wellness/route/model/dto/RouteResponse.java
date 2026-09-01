package com.kh.wellness.route.model.dto;

import java.util.List;

import com.kh.wellness.route.model.vo.TransportType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {

    private TransportType transportType;
    private String selectedOption;
    private PlaceResponse origin;
    private PlaceResponse destination;
    private List<PlaceResponse> waypoints;
    private List<RouteResultResponse> routes;
}
