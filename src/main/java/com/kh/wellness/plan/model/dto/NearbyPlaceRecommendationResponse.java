package com.kh.wellness.plan.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyPlaceRecommendationResponse {

    private Long placeNo;
    private String placeName;
    private String placeDescription;
    private String addr;
    private String addrDetail;
    private Double xAxis;
    private Double yAxis;
    private Long typeNo;
    private String type;
    private Long typeDetailNo;
    private String typeDetail;
    private String imageUrl;
    private Long distanceMeters;
}
