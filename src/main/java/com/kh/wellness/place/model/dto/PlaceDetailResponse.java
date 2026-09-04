package com.kh.wellness.place.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceDetailResponse {

    private Long placeNo;
    private String placeName;
    private String placeDescription;
    private String addr;
    private String addrDetail;
    private String phone;
    private String type;
    private String typeDetail;
    private Double xAxis;
    private Double yAxis;
    private List<PlaceImageResponse> placeImages;
}
