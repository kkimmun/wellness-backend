package com.kh.wellness.route.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapPlaceResponse {

    private Long placeNo;
    private String placeName;
    private String placeDescription;
    private String addr;
    private String addrDetail;
    private String phone;
    private String type;
    private Long viewCount;
    private Double xAxis;
    private Double yAxis;
}
