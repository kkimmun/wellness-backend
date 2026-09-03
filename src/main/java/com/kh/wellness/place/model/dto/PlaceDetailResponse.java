package com.kh.wellness.place.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PlaceDetailResponse {
    private Long placeNo;
    private String placeName;
    private String placeDescription;
    private String addr;
    private String addrDetail;
    private String phoneNumber;
    private Integer viewCount;
    private Double xAxis;
    private Double yAxis;
    private Long typeDetailNo;
    private String typeDetail;
    private String type;
    
    private Boolean isBookmarked;
    private Double avgRating;
    private Integer reviewCount;
    
    private List<PlaceImageDto> placeImages;
    private List<PlaceTagDto> tags;
}
