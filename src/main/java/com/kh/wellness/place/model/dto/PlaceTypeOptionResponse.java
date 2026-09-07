package com.kh.wellness.place.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceTypeOptionResponse {

    private Long typeNo;
    private String type;
    private Long typeDetailNo;
    private String typeDetailContent;
}
