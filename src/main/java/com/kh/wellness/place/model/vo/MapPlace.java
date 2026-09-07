package com.kh.wellness.place.model.vo;

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
public class MapPlace {

    private Long placeNo;
    private String placeName;
    private String placeDescription;
    private String addr;
    private String addrDetail;
    private String phone;
    private String type;
    private String typeDetail;
    private Long viewCount;
    private Double xAxis;
    private Double yAxis;
    private String imageUrl;
}
