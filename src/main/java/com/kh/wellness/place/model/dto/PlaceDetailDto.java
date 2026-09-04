package com.kh.wellness.place.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class PlaceDetailDto {
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
    
    private String bookmarkYn;
    private Double avgRating;
    private Integer reviewCount;
    
    private List<PlaceImageDto> placeImages;
    private List<PlaceTagDto> tags;
}
