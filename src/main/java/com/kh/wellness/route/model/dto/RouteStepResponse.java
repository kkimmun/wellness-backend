package com.kh.wellness.route.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteStepResponse {

    private String type;
    private String guidance;
    private Integer distance;
    private Integer time;
    private List<String> stopNames;
    private List<String> vehicleNames;
    private List<CoordinateResponse> path;
}
