package com.kh.wellness.place.model.dto;

import lombok.Data;

@Data
public class PlaceImageDto {
    private Long imgNo;
    private Long placeNo;
    private String originalName;
    private String saveName;
    private String imgPath;
    private Integer imgOrder;
}
