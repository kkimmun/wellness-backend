package com.kh.wellness.route.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceResponse {

    private Long placeNo;
    private String placeName;
    private String address;

    private Double xAxis;

    private Double yAxis;

    @JsonProperty("X_AXIS")
    public Double getXAxis() {
        return xAxis;
    }

    @JsonProperty("Y_AXIS")
    public Double getYAxis() {
        return yAxis;
    }
}
