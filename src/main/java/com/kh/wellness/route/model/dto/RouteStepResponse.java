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
    // 대중교통 상세 안내용 구조화 필드: 프론트가 guidance 문자열을 파싱하지 않도록 제공한다.
    private List<String> vehicleTypes;
    private String boardingPlace;
    private String alightingPlace;
    private String direction;
    private Integer stopCount;
    private Boolean transfer;
    private List<CoordinateResponse> path;
}
