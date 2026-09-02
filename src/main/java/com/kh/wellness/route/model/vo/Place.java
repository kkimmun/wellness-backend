package com.kh.wellness.route.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Place {

    private Long placeNo;
    private String placeName;
    private String address;
    private Double xAxis;
    private Double yAxis;
}
